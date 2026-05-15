package com.expirytracker.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object DateFormats {
    val display: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    private val iso: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val relaxedPatterns = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("MM-yyyy"),
        DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)
    )

    fun toDb(date: LocalDate): String = iso.format(date)
    fun fromDb(value: String): LocalDate = LocalDate.parse(value, iso)
    fun fromEpochMillis(value: Long): LocalDate = Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
    fun toStartOfDayMillis(date: LocalDate): Long = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun parseRelaxed(value: String): LocalDate? {
        val cleaned = value.trim().replace(".", "/")
        relaxedPatterns.forEach { formatter ->
            try {
                return when (formatter.toString().contains("MonthOfYear")) {
                    true -> LocalDate.parse("01-$cleaned", DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    false -> LocalDate.parse(cleaned, formatter)
                }
            } catch (_: DateTimeParseException) {
            }
        }
        return null
    }
}
