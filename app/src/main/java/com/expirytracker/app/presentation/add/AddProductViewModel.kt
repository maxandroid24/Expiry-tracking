package com.expirytracker.app.presentation.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expirytracker.app.domain.model.Product
import com.expirytracker.app.domain.repository.ProductRepository
import com.expirytracker.app.domain.usecase.SaveProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val saveProduct: SaveProductUseCase
) : ViewModel() {
    private val productId: Long = savedStateHandle["productId"] ?: 0L
    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState = _uiState.asStateFlow()

    init {
        if (productId != 0L) {
            viewModelScope.launch {
                productRepository.getProduct(productId)?.let { _uiState.value = _uiState.value.copy(product = it) }
            }
        }
    }

    fun save(name: String, category: String, manufactured: LocalDate?, expiry: LocalDate?, notes: String) {
        if (manufactured == null || expiry == null) {
            _uiState.value = _uiState.value.copy(error = "Choose manufacturer and expiry dates")
            return
        }
        val existing = _uiState.value.product
        val product = Product(
            id = existing?.id ?: 0L,
            name = name,
            category = category,
            manufacturedDate = manufactured,
            expiryDate = expiry,
            notes = notes,
            imagePath = existing?.imagePath,
            createdAtMillis = existing?.createdAtMillis ?: System.currentTimeMillis()
        )
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            saveProduct(product)
                .onSuccess { _uiState.value = _uiState.value.copy(isSaving = false, saved = true) }
                .onFailure { _uiState.value = _uiState.value.copy(isSaving = false, error = it.message) }
        }
    }
}

data class AddProductUiState(
    val product: Product? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)
