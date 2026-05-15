package com.expirytracker.app.domain.usecase

import com.expirytracker.app.domain.repository.ProductRepository
import com.expirytracker.app.domain.repository.ReminderScheduler
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(id: Long) {
        productRepository.deleteProduct(id)
        reminderScheduler.cancel(id)
    }
}
