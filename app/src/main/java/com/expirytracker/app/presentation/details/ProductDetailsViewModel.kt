package com.expirytracker.app.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expirytracker.app.domain.model.Product
import com.expirytracker.app.domain.repository.ProductRepository
import com.expirytracker.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val productId: Long = savedStateHandle["productId"] ?: 0L
    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val threshold = settingsRepository.expiringSoonThresholdDays.first()
            val product = productRepository.getProduct(productId)
            _uiState.value = ProductDetailsUiState(product = product, thresholdDays = threshold)
        }
    }
}

data class ProductDetailsUiState(
    val product: Product? = null,
    val thresholdDays: Int = 7,
    val today: LocalDate = LocalDate.now()
)
