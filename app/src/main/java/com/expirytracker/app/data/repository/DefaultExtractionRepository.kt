package com.expirytracker.app.data.repository

import android.net.Uri
import com.expirytracker.app.data.ocr.HeuristicProductParser
import com.expirytracker.app.data.ocr.OnlineProductParser
import com.expirytracker.app.data.ocr.TextExtractor
import com.expirytracker.app.domain.model.ExtractedProduct
import com.expirytracker.app.domain.model.ExtractionSource
import com.expirytracker.app.domain.repository.ExtractionRepository
import javax.inject.Inject

class DefaultExtractionRepository @Inject constructor(
    private val textExtractor: TextExtractor,
    private val onlineParser: OnlineProductParser,
    private val fallbackParser: HeuristicProductParser
) : ExtractionRepository {
    override suspend fun extract(uri: Uri): Result<ExtractedProduct> {
        val rawText = textExtractor.extractText(uri).getOrElse { return Result.failure(it) }
        return onlineParser.parse(rawText).recoverCatching {
            fallbackParser.parse(rawText, ExtractionSource.OFFLINE_OCR)
        }
    }
}
