package com.expirytracker.app.presentation.home

import com.expirytracker.app.domain.model.FreshnessStatus

data class ProductListItem(
    val id: Long,
    val name: String,
    val category: String,
    val imagePath: String?,
    val countdown: String,
    val status: FreshnessStatus
)
