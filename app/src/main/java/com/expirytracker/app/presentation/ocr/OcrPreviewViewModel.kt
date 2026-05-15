package com.expirytracker.app.presentation.ocr

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expirytracker.app.domain.model.ExtractedProduct
import com.expirytracker.app.domain.model.Product
import com.expirytracker.app.domain.repository.ExtractionRepository
import com.expirytracker.app.domain.repository.ImageStorage
import com.expirytracker.app.domain.usecase.SaveProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OcrPreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val extractionRepository: ExtractionRepository,
    private val imageStorage: ImageStorage,
    private val saveProduct: SaveProductUseCase
) : ViewModel() {
    val imageUri: Uri? = savedStateHandle.get<String>("imageUri")?.let(Uri::parse)
    private val _uiState = MutableStateFlow(OcrPreviewUiState(isLoading = imageUri != null))
    val uiState = _uiState.asStateFlow()

    init {
        imageUri?.let(::extract)
    }

    private fun extract(uri: Uri) {
        viewModelScope.launch {
            extractionRepository.extract(uri)
                .onSuccess { _uiState.value = OcrPreviewUiState(extractedProduct = it) }
                .onFailure { _uiState.value = OcrPreviewUiState(error = it.message ?: "Could not read the image") }
        }
    }

    fun save(name: String, category: String, manufactured: LocalDate?, expiry: LocalDate?, notes: String) {
        if (manufactured == null || expiry == null) {
            _uiState.value = _uiState.value.copy(error = "Choose manufacturer and expiry dates")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val imagePath = imageUri?.let { imageStorage.persistProductImage(it) }
            saveProduct(Product(name = name, category = category, manufacturedDate = manufactured, expiryDate = expiry, notes = notes, imagePath = imagePath))
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false, saved = true) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
        }
    }
}

data class OcrPreviewUiState(
    val isLoading: Boolean = false,
    val extractedProduct: ExtractedProduct? = null,
    val saved: Boolean = false,
    val error: String? = null
)
