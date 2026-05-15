package com.expirytracker.app.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MlKitTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) : TextExtractor {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extractText(uri: Uri): Result<String> = runCatching {
        val image = InputImage.fromFilePath(context, uri)
        recognizer.process(image).await().text
    }
}
