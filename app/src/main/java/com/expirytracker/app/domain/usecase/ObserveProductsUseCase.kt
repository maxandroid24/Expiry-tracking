package com.expirytracker.app.domain.usecase

import com.expirytracker.app.domain.model.Product
import com.expirytracker.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = productRepository.observeProducts()
}
