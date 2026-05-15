package com.expirytracker.app.domain.repository

import android.net.Uri
import com.expirytracker.app.domain.model.ExtractedProduct

interface ExtractionRepository {
    suspend fun extract(uri: Uri): Result<ExtractedProduct>
}
