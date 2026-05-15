package com.expirytracker.app.presentation.home

data class HomeUiState(
    val products: List<ProductListItem> = emptyList(),
    val totalCount: Int = 0,
    val expiredCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val isLoading: Boolean = true
)
