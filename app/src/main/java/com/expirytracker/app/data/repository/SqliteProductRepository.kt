package com.expirytracker.app.data.repository

import com.expirytracker.app.data.local.ExpiryTrackerDbHelper
import com.expirytracker.app.data.local.ProductContract
import com.expirytracker.app.data.mapper.toContentValues
import com.expirytracker.app.data.mapper.toProduct
import com.expirytracker.app.domain.model.Product
import com.expirytracker.app.domain.repository.ProductRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SqliteProductRepository @Inject constructor(
    private val dbHelper: ExpiryTrackerDbHelper,
    private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {
    private val products = MutableStateFlow<List<Product>>(emptyList())

    init {
        runBlocking { refresh() }
    }

    override fun observeProducts(): Flow<List<Product>> = products

    override suspend fun getProduct(id: Long): Product? = withContext(ioDispatcher) {
        dbHelper.readableDatabase.query(
            ProductContract.Products.TABLE,
            null,
            "${ProductContract.Products.ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor -> if (cursor.moveToFirst()) cursor.toProduct() else null }
    }

    override suspend fun addProduct(product: Product): Long = withContext(ioDispatcher) {
        val id = dbHelper.writableDatabase.insertOrThrow(ProductContract.Products.TABLE, null, product.toContentValues())
        refresh()
        id
    }

    override suspend fun updateProduct(product: Product) = withContext(ioDispatcher) {
        dbHelper.writableDatabase.update(
            ProductContract.Products.TABLE,
            product.toContentValues(),
            "${ProductContract.Products.ID} = ?",
            arrayOf(product.id.toString())
        )
        refresh()
    }

    override suspend fun deleteProduct(id: Long) = withContext(ioDispatcher) {
        dbHelper.writableDatabase.delete(ProductContract.Products.TABLE, "${ProductContract.Products.ID} = ?", arrayOf(id.toString()))
        refresh()
    }

    suspend fun refresh() = withContext(ioDispatcher) {
        val all = mutableListOf<Product>()
        dbHelper.readableDatabase.query(
            ProductContract.Products.TABLE,
            null,
            null,
            null,
            null,
            null,
            "${ProductContract.Products.EXPIRY_DATE} ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) all += cursor.toProduct()
        }
        products.value = all
    }
}
