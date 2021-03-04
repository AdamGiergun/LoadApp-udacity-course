package com.udacity

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application): AndroidViewModel(application) {

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        }
    }

    private lateinit var uri: Uri

    fun setDownloadUri(id: Int) {
        uri = Uri.parse(when (id) {
            R.id.glide_radio -> "https://github.com/bumptech/glide/archive/master.zip"
            R.id.loadapp_radio -> "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
            R.id.retrofit_radio -> "https://github.com/square/retrofit/archive/master.zip"
            else -> ""
        })
    }

    private val app = getApplication<Application>()
    private var downloadID: Long = 0

    fun download() {
        val request =
            DownloadManager.Request(uri)
                .setTitle(app.getString(R.string.app_name))
                .setDescription(app.getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = app.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }
}