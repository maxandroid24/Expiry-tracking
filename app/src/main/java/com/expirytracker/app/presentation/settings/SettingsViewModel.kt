package com.expirytracker.app.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expirytracker.app.domain.repository.BackupRepository
import com.expirytracker.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {
    val threshold = settingsRepository.expiringSoonThresholdDays.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)
    val message = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    fun setThreshold(days: Int) {
        viewModelScope.launch { settingsRepository.setExpiringSoonThresholdDays(days) }
    }

    fun exportBackup(destination: Uri) {
        viewModelScope.launch {
            backupRepository.exportBackup(destination)
                .onSuccess { message.value = "Backup exported" }
                .onFailure { message.value = it.message ?: "Backup failed" }
        }
    }

    fun importBackup(source: Uri) {
        viewModelScope.launch {
            backupRepository.importBackup(source)
                .onSuccess { message.value = "Backup restored" }
                .onFailure { message.value = it.message ?: "Restore failed" }
        }
    }
}
