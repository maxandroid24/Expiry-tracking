package com.expirytracker.app.presentation.common

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.expirytracker.app.R
import com.expirytracker.app.domain.model.FreshnessStatus

fun TextView.bindStatus(status: FreshnessStatus) {
    text = when (status) {
        FreshnessStatus.FRESH -> "Fresh"
        FreshnessStatus.EXPIRING_SOON -> "Soon"
        FreshnessStatus.EXPIRED -> "Expired"
    }
    setBackgroundResource(
        when (status) {
            FreshnessStatus.FRESH -> R.drawable.bg_status_fresh
            FreshnessStatus.EXPIRING_SOON -> R.drawable.bg_status_soon
            FreshnessStatus.EXPIRED -> R.drawable.bg_status_expired
        }
    )
}

fun View.tintForStatus(status: FreshnessStatus) {
    val color = when (status) {
        FreshnessStatus.FRESH -> R.color.fresh
        FreshnessStatus.EXPIRING_SOON -> R.color.soon
        FreshnessStatus.EXPIRED -> R.color.expired
    }
    backgroundTintList = ContextCompat.getColorStateList(context, color)
}
