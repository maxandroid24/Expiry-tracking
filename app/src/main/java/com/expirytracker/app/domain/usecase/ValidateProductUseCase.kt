package com.expirytracker.app.domain.usecase

import com.expirytracker.app.domain.model.Product

class ValidateProductUseCase {
    operator fun invoke(product: Product): ProductValidationResult {
        val errors = mutableMapOf<ProductField, String>()
        if (product.name.isBlank()) errors[ProductField.NAME] = "Enter a product name"
        if (product.category.isBlank()) errors[ProductField.CATEGORY] = "Enter a category"
        if (product.expiryDate.isBefore(product.manufacturedDate)) {
            errors[ProductField.EXPIRY_DATE] = "Expiry date must be after manufacturer date"
        }
        return ProductValidationResult(errors)
    }
}

data class ProductValidationResult(val errors: Map<ProductField, String>) {
    val isValid: Boolean = errors.isEmpty()
}

enum class ProductField {
    NAME,
    CATEGORY,
    MANUFACTURED_DATE,
    EXPIRY_DATE
}
