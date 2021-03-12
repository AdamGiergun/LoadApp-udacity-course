package com.udacity

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class MainViewModel(application: Application): AndroidViewModel(application) {

    private val _downloadButtonState = MutableLiveData<ButtonState>().apply {
        value = ButtonState.Inactive
    }
    val downloadButtonState: LiveData<ButtonState>
        get() = _downloadButtonState

    fun completedStateConsumed() {
        _downloadButtonState.value = ButtonState.Active
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadID) _downloadButtonState.value = ButtonState.Completed
        }
    }

    private var downloadOptionId = -1
    private lateinit var uri: Uri

    fun setDownloadOptionId(id: Int) {
        if (downloadOptionId != id) {
            _downloadButtonState.value = ButtonState.Active
            downloadOptionId = id
            uri = Uri.parse(when (id) {
                R.id.glide_radio -> "https://github.com/bumptech/glide/archive/master.zip"
                R.id.loadapp_radio -> "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
                R.id.retrofit_radio -> "https://github.com/square/retrofit/archive/master.zip"
                else -> ""
            })
        }
    }

    private val app = getApplication<Application>()
    private var downloadID: Long = 0

    fun download() {
        _downloadButtonState.value = ButtonState.Loading
        val fileName = uri.pathSegments[uri.pathSegments.size - 3]
        val downloadDir = ContextCompat.getExternalFilesDirs(app, Environment.DIRECTORY_DOWNLOADS)[0]

        val file = File("${downloadDir.path}/$fileName")

        val request =
            DownloadManager.Request(uri).apply {
                setDestinationUri(Uri.fromFile(file))
                setTitle(fileName)
                setDescription(app.getString(R.string.app_description))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setRequiresCharging(false)
                }
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

        val downloadManager = ContextCompat.getSystemService(app, DownloadManager::class.java)
        downloadID =
            downloadManager?.enqueue(request) ?: -1 // enqueue puts the download request in the queue.
    }
}