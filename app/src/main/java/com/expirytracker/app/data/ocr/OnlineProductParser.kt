package com.expirytracker.app.data.ocr

import com.expirytracker.app.domain.model.ExtractedProduct

interface OnlineProductParser {
    suspend fun parse(rawText: String): Result<ExtractedProduct>
}
