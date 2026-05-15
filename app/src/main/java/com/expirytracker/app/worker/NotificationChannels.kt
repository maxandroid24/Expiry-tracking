package com.expirytracker.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.expirytracker.app.R

object NotificationChannels {
    const val EXPIRY_REMINDERS = "expiry_reminders"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return
        val channel = NotificationChannel(
            EXPIRY_REMINDERS,
            context.getString(R.string.channel_expiry_reminders),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_expiry_reminders_description)
        }
        manager.createNotificationChannel(channel)
    }
}
