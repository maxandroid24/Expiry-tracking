package com.expirytracker.app.data.backup

import android.content.Context
import android.net.Uri
import com.expirytracker.app.domain.model.Product
import com.expirytracker.app.domain.repository.BackupRepository
import com.expirytracker.app.domain.repository.ProductRepository
import com.expirytracker.app.util.DateFormats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class JsonBackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productRepository: ProductRepository,
    private val ioDispatcher: CoroutineDispatcher
) : BackupRepository {
    override suspend fun exportBackup(destination: Uri): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val products = productRepository.observeProducts().first()
            val json = JSONArray(products.map { it.toJson() })
            context.contentResolver.openOutputStream(destination)?.use { output ->
                output.write(json.toString(2).toByteArray())
            }
        }
    }

    override suspend fun importBackup(source: Uri): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val content = context.contentResolver.openInputStream(source)?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = JSONArray(content)
            for (index in 0 until json.length()) {
                productRepository.addProduct(json.getJSONObject(index).toProduct())
            }
        }
    }

    private fun Product.toJson(): JSONObject = JSONObject()
        .put("name", name)
        .put("category", category)
        .put("manufacturedDate", DateFormats.toDb(manufacturedDate))
        .put("expiryDate", DateFormats.toDb(expiryDate))
        .put("notes", notes)
        .put("imagePath", imagePath)
        .put("createdAtMillis", createdAtMillis)

    private fun JSONObject.toProduct(): Product = Product(
        name = getString("name"),
        category = getString("category"),
        manufacturedDate = DateFormats.fromDb(getString("manufacturedDate")),
        expiryDate = DateFormats.fromDb(getString("expiryDate")),
        notes = optString("notes"),
        imagePath = optString("imagePath").takeIf { it.isNotBlank() },
        createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis())
    )
}
