package com.expirytracker.app.domain.model

import java.time.LocalDate

data class ExtractedProduct(
    val name: String? = null,
    val category: String? = null,
    val manufacturedDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
    val rawText: String = "",
    val source: ExtractionSource = ExtractionSource.OFFLINE_OCR
)

enum class ExtractionSource {
    ONLINE_AI,
    OFFLINE_OCR
}
