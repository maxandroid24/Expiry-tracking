package com.expirytracker.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val expiringSoonThresholdDays: Flow<Int>
    suspend fun setExpiringSoonThresholdDays(days: Int)
}
