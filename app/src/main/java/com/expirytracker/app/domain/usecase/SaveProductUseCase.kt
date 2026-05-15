package com.expirytracker.app.domain.usecase

import com.expirytracker.app.domain.model.Product
import com.expirytracker.app.domain.repository.ProductRepository
import com.expirytracker.app.domain.repository.ReminderScheduler
import javax.inject.Inject

class SaveProductUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val reminderScheduler: ReminderScheduler,
    private val validateProduct: ValidateProductUseCase
) {
    suspend operator fun invoke(product: Product): Result<Long> {
        val validation = validateProduct(product)
        if (!validation.isValid) return Result.failure(IllegalArgumentException(validation.errors.values.first()))
        val id = if (product.id == 0L) productRepository.addProduct(product) else {
            productRepository.updateProduct(product)
            product.id
        }
        reminderScheduler.schedule(product.copy(id = id))
        return Result.success(id)
    }
}
