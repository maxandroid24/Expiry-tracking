package com.expirytracker.app.data.mapper

import android.content.ContentValues
import android.database.Cursor
import com.expirytracker.app.data.local.ProductContract
import com.expirytracker.app.domain.model.Product
import com.expirytracker.app.util.DateFormats

fun Product.toContentValues(): ContentValues = ContentValues().apply {
    if (id != 0L) put(ProductContract.Products.ID, id)
    put(ProductContract.Products.NAME, name.trim())
    put(ProductContract.Products.CATEGORY, category.trim())
    put(ProductContract.Products.MANUFACTURED_DATE, DateFormats.toDb(manufacturedDate))
    put(ProductContract.Products.EXPIRY_DATE, DateFormats.toDb(expiryDate))
    put(ProductContract.Products.NOTES, notes.trim())
    put(ProductContract.Products.IMAGE_PATH, imagePath)
    put(ProductContract.Products.CREATED_AT, createdAtMillis)
    put(ProductContract.Products.UPDATED_AT, System.currentTimeMillis())
}

fun Cursor.toProduct(): Product = Product(
    id = getLong(getColumnIndexOrThrow(ProductContract.Products.ID)),
    name = getString(getColumnIndexOrThrow(ProductContract.Products.NAME)),
    category = getString(getColumnIndexOrThrow(ProductContract.Products.CATEGORY)),
    manufacturedDate = DateFormats.fromDb(getString(getColumnIndexOrThrow(ProductContract.Products.MANUFACTURED_DATE))),
    expiryDate = DateFormats.fromDb(getString(getColumnIndexOrThrow(ProductContract.Products.EXPIRY_DATE))),
    notes = getString(getColumnIndexOrThrow(ProductContract.Products.NOTES)),
    imagePath = getString(getColumnIndexOrThrow(ProductContract.Products.IMAGE_PATH)),
    createdAtMillis = getLong(getColumnIndexOrThrow(ProductContract.Products.CREATED_AT)),
    updatedAtMillis = getLong(getColumnIndexOrThrow(ProductContract.Products.UPDATED_AT))
)
