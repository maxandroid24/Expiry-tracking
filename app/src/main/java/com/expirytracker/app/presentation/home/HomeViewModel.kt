package com.expirytracker.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expirytracker.app.domain.model.FreshnessStatus
import com.expirytracker.app.domain.model.Product
import com.expirytracker.app.domain.model.ProductFilter
import com.expirytracker.app.domain.model.ProductSort
import com.expirytracker.app.domain.repository.SettingsRepository
import com.expirytracker.app.domain.usecase.DeleteProductUseCase
import com.expirytracker.app.domain.usecase.ObserveProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeProducts: ObserveProductsUseCase,
    private val deleteProduct: DeleteProductUseCase,
    settingsRepository: SettingsRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(ProductFilter.ALL)
    private val sort = MutableStateFlow(ProductSort.EXPIRY_ASC)

    val uiState = combine(
        observeProducts(),
        settingsRepository.expiringSoonThresholdDays,
        query,
        filter,
        sort
    ) { products, threshold, search, activeFilter, activeSort ->
        val today = LocalDate.now()
        val statuses = products.associateWith { it.freshnessStatus(today, threshold.toLong()) }
        val visible = products
            .filter { it.name.contains(search, true) || it.category.contains(search, true) }
            .filter { activeFilter == ProductFilter.ALL || statuses[it]?.name == activeFilter.name }
            .sortedWith(sortComparator(activeSort, statuses))
        HomeUiState(
            products = visible.map { it.toListItem(statuses.getValue(it), today) },
            totalCount = products.size,
            expiredCount = statuses.values.count { it == FreshnessStatus.EXPIRED },
            expiringSoonCount = statuses.values.count { it == FreshnessStatus.EXPIRING_SOON },
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun search(value: String) {
        query.value = value
    }

    fun setFilter(value: ProductFilter) {
        filter.value = value
    }

    fun cycleSort() {
        sort.value = when (sort.value) {
            ProductSort.EXPIRY_ASC -> ProductSort.EXPIRY_DESC
            ProductSort.EXPIRY_DESC -> ProductSort.FRESHNESS
            ProductSort.FRESHNESS -> ProductSort.EXPIRY_ASC
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { deleteProduct(id) }
    }

    private fun Product.toListItem(status: FreshnessStatus, today: LocalDate): ProductListItem {
        val days = ChronoUnit.DAYS.between(today, expiryDate)
        val countdown = when {
            days < 0 -> "Expired ${-days} day${if (days == -1L) "" else "s"} ago"
            days == 0L -> "Expires today"
            days == 1L -> "Expires tomorrow"
            else -> "$days days left"
        }
        return ProductListItem(id, name, category, imagePath, countdown, status)
    }

    private fun sortComparator(sort: ProductSort, statuses: Map<Product, FreshnessStatus>): Comparator<Product> = when (sort) {
        ProductSort.EXPIRY_ASC -> compareBy { it.expiryDate }
        ProductSort.EXPIRY_DESC -> compareByDescending { it.expiryDate }
        ProductSort.FRESHNESS -> compareBy { statuses[it]?.ordinal ?: 0 }
    }
}
