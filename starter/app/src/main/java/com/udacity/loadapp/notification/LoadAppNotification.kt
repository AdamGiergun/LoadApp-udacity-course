package com.udacity.loadapp.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.udacity.loadapp.Download
import com.udacity.loadapp.R
import com.udacity.loadapp.getDownloadStatusStringId
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
        val pendingIntent =
            PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_IMMUTABLE)

        val download: Download? = intent.getParcelableExtra(Download.EXTRA_NAME)

        NotificationCompat.Builder(context, LoadAppNotificationChannel.CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_assistant_black_24dp)
            setContentTitle(context.getString(R.string.notification_title))
            setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    context.getString(
                        R.string.notification_description,
                        context.getString(download?.details ?: R.string.unknown),
                        context.getString(
                            if (download == null)
                                R.string.unknown
                            else
                                getDownloadStatusStringId(download.status)
                        )
                    )
                )
            )
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