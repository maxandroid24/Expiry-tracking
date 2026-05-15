package com.expirytracker.app.domain.repository

import com.expirytracker.app.domain.model.Product

interface ReminderScheduler {
    fun schedule(product: Product)
    fun cancel(productId: Long)
}
