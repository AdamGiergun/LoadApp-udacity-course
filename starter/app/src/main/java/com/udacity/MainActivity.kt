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

    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(layoutInflater).run {
            binding = this
            setContentView(root)
            setSupportActionBar(toolbar)

            contentMain.run {
                downloadChooser.setOnCheckedChangeListener { _, checkedId ->
                    viewModel.setDownloadOptionId(checkedId)
                }
                downloadButton.setOnClickListener {
                    viewModel.downloadButtonClicked()
                }
                viewModel.downloadButtonState.observe(this@MainActivity) {
                    downloadButton.setState(it)
                }
            }
        }

        registerReceiver(
            viewModel.receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        viewModel.showInfo.observe(this) {
            if (it) Toast.makeText(this, viewModel.infoId, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val CHANNEL_ID = "channelId"
    }
}