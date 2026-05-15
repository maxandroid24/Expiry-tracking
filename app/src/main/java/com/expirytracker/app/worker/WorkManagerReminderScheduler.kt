package com.expirytracker.app.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.expirytracker.app.domain.model.Product
import com.expirytracker.app.domain.repository.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkManagerReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : ReminderScheduler {
    override fun schedule(product: Product) {
        val reminderTime = product.expiryDate.minusDays(1).atTime(LocalTime.of(10, 0))
        val delayMillis = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
            LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (delayMillis <= 0L) return
        val request = OneTimeWorkRequestBuilder<ExpiryReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putLong(ExpiryReminderWorker.KEY_PRODUCT_ID, product.id)
                    .putString(ExpiryReminderWorker.KEY_PRODUCT_NAME, product.name)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(product.id), ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancel(productId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(productId))
    }

    private fun workName(productId: Long): String = "expiry_reminder_$productId"
}
