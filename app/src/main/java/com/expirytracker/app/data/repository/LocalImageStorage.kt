package com.expirytracker.app.data.repository

import android.content.Context
import android.net.Uri
import com.expirytracker.app.domain.repository.ImageStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class LocalImageStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : ImageStorage {
    override suspend fun persistProductImage(source: Uri): String? = withContext(ioDispatcher) {
        runCatching {
            val directory = File(context.filesDir, "product_images").apply { mkdirs() }
            val file = File(directory, "product_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(source)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.absolutePath
        }.getOrNull()
    }
}
