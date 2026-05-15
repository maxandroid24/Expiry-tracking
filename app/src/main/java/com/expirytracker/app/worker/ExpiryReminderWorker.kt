package com.expirytracker.app.worker

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.expirytracker.app.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ExpiryReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val productId = inputData.getLong(KEY_PRODUCT_ID, -1)
        val productName = inputData.getString(KEY_PRODUCT_NAME) ?: return Result.failure()
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return Result.success()
        }
        NotificationChannels.ensureCreated(applicationContext)
        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.EXPIRY_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Expiry reminder")
            .setContentText("$productName expires tomorrow.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        applicationContext.getSystemService<NotificationManager>()?.notify(productId.toInt(), notification)
        return Result.success()
    }

    companion object {
        const val KEY_PRODUCT_ID = "product_id"
        const val KEY_PRODUCT_NAME = "product_name"
    }
}
