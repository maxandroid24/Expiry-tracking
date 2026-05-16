package com.expirytracker.app.data.ocr

import com.expirytracker.app.BuildConfig
import com.expirytracker.app.domain.model.ExtractedProduct
import com.expirytracker.app.domain.model.ExtractionSource
import com.expirytracker.app.util.DateFormats
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONArray
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
            Log.i("GeminiParser", "Attempting extraction with Gemini AI...")
            require(BuildConfig.GEMINI_API_KEY.isNotBlank()) { "Gemini API key is not configured" }
            val prompt = """
                Extract product expiry data as compact JSON only.
                Keys: name, category, manufacturedDate, expiryDate.
                Dates must be yyyy-MM-dd or null.
                Note: In India, labels like 'Mfg Date', 'Pkd', 'Packed', 'Best Before', 'Exp. Date', 'Use By' are common.
                Text:
                $rawText
            """.trimIndent()
            val body = JSONObject()
                .put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(
                        JSONObject().put("text", prompt)
                    ))
                )).toString()

            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=${BuildConfig.GEMINI_API_KEY}")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 12000
                readTimeout = 20000
                doOutput = true
            }
            connection.outputStream.use { it.write(body.toByteArray()) }
            
            if (connection.responseCode != 200) {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e("GeminiParser", "API Error ${connection.responseCode}: $errorBody")
                throw Exception("Gemini API error: ${connection.responseCode}")
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d("GeminiParser", "Full API Response: $response")
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
            Log.i("GeminiParser", "Successfully extracted product using Gemini AI")
            ExtractedProduct(
                name = json.optString("name").takeIf { it.isNotBlank() && it != "null" },
                category = json.optString("category").takeIf { it.isNotBlank() && it != "null" },
                manufacturedDate = json.optString("manufacturedDate").takeIf { it.isNotBlank() && it != "null" }?.let(DateFormats::fromDb),
                expiryDate = json.optString("expiryDate").takeIf { it.isNotBlank() && it != "null" }?.let(DateFormats::fromDb),
                rawText = rawText,
                source = ExtractionSource.ONLINE_AI
            )
        }.recoverCatching { e ->
            Log.w("GeminiParser", "Gemini failed: ${e.message}. Falling back to On-device OCR.")
            heuristicParser.parse(rawText, ExtractionSource.OFFLINE_OCR)
        }
    }
}
