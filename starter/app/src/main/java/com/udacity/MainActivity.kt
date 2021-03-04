package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
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

            registerReceiver(
                viewModel.receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )

            contentMain.customButton.setOnClickListener {
                showInfo()
                //viewModel.download()
            }
        }
    }

    private fun showInfo() {
        Toast.makeText(this, "Please, choose what to download first", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val CHANNEL_ID = "channelId"
    }
}