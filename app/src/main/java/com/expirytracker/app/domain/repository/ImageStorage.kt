package com.expirytracker.app.domain.repository

import android.net.Uri

interface ImageStorage {
    suspend fun persistProductImage(source: Uri): String?
}
