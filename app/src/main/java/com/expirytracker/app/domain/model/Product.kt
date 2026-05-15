package com.expirytracker.app.domain.model

import java.time.LocalDate

data class Product(
    val id: Long = 0,
    val name: String,
    val category: String,
    val manufacturedDate: LocalDate,
    val expiryDate: LocalDate,
    val notes: String = "",
    val imagePath: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun freshnessStatus(today: LocalDate, soonThresholdDays: Long): FreshnessStatus = when {
        expiryDate.isBefore(today) -> FreshnessStatus.EXPIRED
        !expiryDate.isAfter(today.plusDays(soonThresholdDays)) -> FreshnessStatus.EXPIRING_SOON
        else -> FreshnessStatus.FRESH
    }
}
