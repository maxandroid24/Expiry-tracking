package com.expirytracker.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpiryTrackerDbHelper @Inject constructor(
    @ApplicationContext context: Context
) : SQLiteOpenHelper(
    context,
    ProductContract.DATABASE_NAME,
    null,
    ProductContract.DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${ProductContract.Products.TABLE} (
                ${ProductContract.Products.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ProductContract.Products.NAME} TEXT NOT NULL,
                ${ProductContract.Products.CATEGORY} TEXT NOT NULL,
                ${ProductContract.Products.MANUFACTURED_DATE} TEXT NOT NULL,
                ${ProductContract.Products.EXPIRY_DATE} TEXT NOT NULL,
                ${ProductContract.Products.NOTES} TEXT NOT NULL DEFAULT '',
                ${ProductContract.Products.IMAGE_PATH} TEXT,
                ${ProductContract.Products.CREATED_AT} INTEGER NOT NULL,
                ${ProductContract.Products.UPDATED_AT} INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_products_expiry ON ${ProductContract.Products.TABLE} (${ProductContract.Products.EXPIRY_DATE})")
        db.execSQL("CREATE INDEX idx_products_category ON ${ProductContract.Products.TABLE} (${ProductContract.Products.CATEGORY})")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${ProductContract.Products.TABLE}")
        onCreate(db)
    }
}
