package com.expirytracker.app.data.repository

import android.content.Context
import com.expirytracker.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesSettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) : SettingsRepository {
    private val prefs = context.getSharedPreferences("expiry_settings", Context.MODE_PRIVATE)
    private val threshold = MutableStateFlow(prefs.getInt(KEY_THRESHOLD, 7))

    override val expiringSoonThresholdDays: Flow<Int> = threshold

    override suspend fun setExpiringSoonThresholdDays(days: Int) {
        prefs.edit().putInt(KEY_THRESHOLD, days).apply()
        threshold.value = days
    }

    private companion object {
        const val KEY_THRESHOLD = "threshold_days"
    }
}
