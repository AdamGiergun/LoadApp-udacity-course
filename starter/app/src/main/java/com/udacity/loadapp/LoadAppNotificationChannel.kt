package com.udacity.loadapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build

object LoadAppNotificationChannel {

    const val CHANNEL_ID = "loadAppChannel"
    private const val CHANNEL_NAME = "LoadApp"

    fun create(application: Application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).run {
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                description = application.getString(R.string.channel_for_load_app)
                val notificationManager =
                    application.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(this)
            }
        }
    }
}
