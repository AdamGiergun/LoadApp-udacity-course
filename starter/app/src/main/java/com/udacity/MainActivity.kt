package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(layoutInflater).run {
            setContentView(root)
            setSupportActionBar(toolbar)

            registerReceiver(viewModel.receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

            contentMain.customButton.setOnClickListener {
                viewModel.download()
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "channelId"
    }
}