package com.expirytracker.app.data.ocr

import android.net.Uri

interface TextExtractor {
    suspend fun extractText(uri: Uri): Result<String>
}
