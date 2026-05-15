package com.expirytracker.app.data.local

object ProductContract {
    const val DATABASE_NAME = "expiry_tracker.db"
    const val DATABASE_VERSION = 1

    object Products {
        const val TABLE = "products"
        const val ID = "id"
        const val NAME = "name"
        const val CATEGORY = "category"
        const val MANUFACTURED_DATE = "manufactured_date"
        const val EXPIRY_DATE = "expiry_date"
        const val NOTES = "notes"
        const val IMAGE_PATH = "image_path"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
    }
}
