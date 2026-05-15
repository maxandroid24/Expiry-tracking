package com.expirytracker.app.domain.repository

import android.net.Uri

interface BackupRepository {
    suspend fun exportBackup(destination: Uri): Result<Unit>
    suspend fun importBackup(source: Uri): Result<Unit>
}
