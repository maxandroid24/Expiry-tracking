package com.expirytracker.app.data.ocr

import com.expirytracker.app.BuildConfig
import com.expirytracker.app.domain.model.ExtractedProduct
import com.expirytracker.app.domain.model.ExtractionSource
import com.expirytracker.app.util.DateFormats
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class GeminiProductParser @Inject constructor(
    private val ioDispatcher: CoroutineDispatcher,
    private val heuristicParser: HeuristicProductParser
) : OnlineProductParser {
    override suspend fun parse(rawText: String): Result<ExtractedProduct> = withContext(ioDispatcher) {
        runCatching {
            require(BuildConfig.GEMINI_API_KEY.isNotBlank()) { "Gemini API key is not configured" }
            val prompt = """
                Extract product expiry data as compact JSON only.
                Keys: name, category, manufacturedDate, expiryDate.
                Dates must be yyyy-MM-dd or null.
                Text:
                $rawText
            """.trimIndent()
            val body = JSONObject()
                .put("contents", arrayOf(JSONObject().put("parts", arrayOf(JSONObject().put("text", prompt)))))
                .toString()

            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 12000
                readTimeout = 20000
                doOutput = true
            }
            connection.outputStream.use { it.write(body.toByteArray()) }
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val text = JSONObject(response)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .replace("```json", "")
                .replace("```", "")
                .trim()
            val json = JSONObject(text)
            ExtractedProduct(
                name = json.optString("name").takeIf { it.isNotBlank() },
                category = json.optString("category").takeIf { it.isNotBlank() },
                manufacturedDate = json.optString("manufacturedDate").takeIf { it.isNotBlank() }?.let(DateFormats::fromDb),
                expiryDate = json.optString("expiryDate").takeIf { it.isNotBlank() }?.let(DateFormats::fromDb),
                rawText = rawText,
                source = ExtractionSource.ONLINE_AI
            )
        }.recoverCatching {
            heuristicParser.parse(rawText, ExtractionSource.OFFLINE_OCR)
        }
    }
}
