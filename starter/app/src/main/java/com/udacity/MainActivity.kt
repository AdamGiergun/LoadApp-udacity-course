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

                viewModel.downloadButtonState.observe(this@MainActivity) {
                    downloadButton.setState(it)
                    when (it) {
                        ButtonState.Inactive -> downloadButton.setOnClickListener { showInfo(false) }
                        ButtonState.Active -> downloadButton.setOnClickListener { viewModel.download() }
                        ButtonState.Completed -> downloadButton.setOnClickListener { showInfo(true) }
                        else -> downloadButton.setOnClickListener {}
                    }
                }
            }
        }

        registerReceiver(
            viewModel.receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    private fun showInfo(isDownloadChosen: Boolean) {
        Toast.makeText(
            this,
            getString(
                if (isDownloadChosen)
                    R.string.please_choose_another_download
                else
                    R.string.please_choose_download
            ),
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private const val CHANNEL_ID = "channelId"
    }
}