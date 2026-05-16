package com.expirytracker.app.data.ocr

import com.expirytracker.app.domain.model.ExtractedProduct
import com.expirytracker.app.domain.model.ExtractionSource
import com.expirytracker.app.util.DateFormats
import java.time.LocalDate
import javax.inject.Inject

class HeuristicProductParser @Inject constructor() {
    private val mfgKeywords = listOf("mfg", "mfd", "pkd", "packed", "packing", "manufactured")
    private val expKeywords = listOf("exp", "expiry", "best before", "use by", "expires", "validity")

    fun parse(rawText: String, source: ExtractionSource): ExtractedProduct {
        val lines = rawText.lines().map { it.trim() }.filter { it.length > 2 }
        val dates = DATE_REGEX.findAll(rawText).mapNotNull { DateFormats.parseRelaxed(it.value) }.toList()
        val name = lines.firstOrNull { line -> 
            mfgKeywords.none { line.contains(it, true) } && expKeywords.none { line.contains(it, true) } 
        }
        val category = inferCategory(rawText)
        
        return ExtractedProduct(
            name = name,
            category = category,
            manufacturedDate = findDateNear(rawText, mfgKeywords) ?: dates.minOrNull(),
            expiryDate = findDateNear(rawText, expKeywords) ?: dates.maxOrNull(),
            rawText = rawText,
            source = source
        )
    }

    private fun inferCategory(text: String): String = when {
        text.contains("tablet", true) || text.contains("capsule", true) || text.contains("mg", true) -> "Medicine"
        text.contains("milk", true) || text.contains("bread", true) || text.contains("juice", true) -> "Food"
        text.contains("cream", true) || text.contains("lotion", true) || text.contains("shampoo", true) -> "Cosmetics"
        else -> "General"
    }

    private fun findDateNear(text: String, keywords: List<String>): LocalDate? {
        val lines = text.lines()
        for (keyword in keywords) {
            val line = lines.firstOrNull { it.contains(keyword, true) } ?: continue
            val match = DATE_REGEX.find(line)?.value ?: continue
            val parsed = DateFormats.parseRelaxed(match)
            if (parsed != null) return parsed
        }
        return null
    }

    private companion object {
        val DATE_REGEX = Regex("""\b(\d{1,2}[/-]\d{1,2}[/-]\d{2,4}|\d{1,2}[/-]\d{4}|[A-Za-z]{3,9}\s+\d{4})\b""")
    }
}
