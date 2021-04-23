package com.udacity.loadapp.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.udacity.loadapp.R
import kotlin.math.abs
import kotlin.random.Random

object LoadAppNotification {
    const val EXTRA_ID = "notification_id"

    private var notificationId = getInitial()

    private fun getInitial(): Int {
        return abs(Random.nextInt())
    }

    fun notify(context: Context, intent: Intent) {
        intent.putExtra(EXTRA_ID, notificationId)
        val pendingIntent = PendingIntent.getActivity(context, notificationId, intent, 0)

        NotificationCompat.Builder(context, LoadAppNotificationChannel.CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_assistant_black_24dp)
            setContentTitle(context.getString(R.string.notification_title))
            setContentText(context.getString(R.string.notification_description))
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
            addAction(
                R.drawable.ic_assistant_black_24dp,
                context.getString(R.string.details_cap),
                pendingIntent
            )

            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, build())
            }
            notificationId++
        }
    }
}