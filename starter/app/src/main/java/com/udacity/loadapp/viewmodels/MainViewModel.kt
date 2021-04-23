package com.udacity.loadapp.viewmodels

import android.app.*
import android.app.DownloadManager.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.udacity.loadapp.*
import com.udacity.loadapp.activities.DetailActivity
import com.udacity.loadapp.button.ButtonState
import com.udacity.loadapp.notification.LoadAppNotification
import com.udacity.loadapp.notification.LoadAppNotificationChannel

class MainViewModel(application: Application) : AndroidViewModel(application) {

    init {
        LoadAppNotificationChannel.create(application)
    }

    private val _downloadButtonState = MutableLiveData<ButtonState>(ButtonState.Inactive)
    val downloadButtonState: LiveData<ButtonState>
        get() = _downloadButtonState
    private fun completeDownload() {
        _downloadButtonState.value = ButtonState.Completed
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                ContextCompat.getSystemService(it, DownloadManager::class.java)?.let { downloadManager ->
                    intent?.getDownload(downloadManager).let { download ->
                        val notificationIntent = Intent(context, DetailActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(Download.EXTRA_NAME, download)
                        }
                        LoadAppNotification.notify(it, notificationIntent)
                    }
                }
                it.unregisterReceiver(this)
            }
        }

        private fun Intent.getDownload(downloadManager: DownloadManager): Download {
            var downloadStatus = STATUS_PENDING
            var downloadTitle = ""

            getLongExtra(EXTRA_DOWNLOAD_ID, -1).let { id ->
                if (id == downloadID) completeDownload()

                val query = Query().setFilterById(id)
                downloadManager.query(query).use { cursor ->
                    if (cursor.moveToFirst()) {
                        var columnIndex = cursor.getColumnIndex(COLUMN_STATUS)
                        downloadStatus = cursor.getInt(columnIndex)

                        columnIndex = cursor.getColumnIndex(COLUMN_TITLE)
                        downloadTitle = cursor.getString(columnIndex)
                    }
                }
            }

            val downloadDetails = when {
                downloadTitle.contains("glide") -> R.string.glide_library
                downloadTitle.contains("nd940") -> R.string.loadapp_repository
                downloadTitle.contains("retrofit") -> R.string.retrofit_client
                else -> R.string.unknown
            }

            return Download(downloadTitle, downloadDetails, downloadStatus)
        }
    }

    private var downloadOptionId = -1
    fun RadioGroup.setDownloadOptionId(id: Int) {
        if (downloadOptionId != id) {
            _downloadButtonState.value = ButtonState.Active
            downloadOptionId = id
        }
    }

    private var downloadID: Long = 0
    private val uri
        get() = Uri.parse(
            when (downloadOptionId) {
                R.id.glide_radio -> "https://github.com/bumptech/glide/archive/master.zip"
                R.id.loadapp_radio -> "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
                R.id.retrofit_radio -> "https://github.com/square/retrofit/archive/master.zip"
                else -> ""
            }
        )

    private fun download() {
        _downloadButtonState.value = ButtonState.Loading
        getApplication<Application>().run {

            val title = uri.pathSegments[uri.pathSegments.size - 3]
            val desc = getString(R.string.app_description).replace("files", title)

            val request = Request(uri).apply {
                setTitle(title)
                setDescription(desc)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setRequiresCharging(false)
                }
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            registerReceiver(receiver, IntentFilter(ACTION_DOWNLOAD_COMPLETE))
            val downloadManager = ContextCompat.getSystemService(this, DownloadManager::class.java)
            downloadID = downloadManager?.enqueue(request) ?: -1
        }
    }

    private val _showInfo = MutableLiveData<Boolean>()
    val showInfo: LiveData<Boolean>
        get() = _showInfo

    val infoId
        get() = if (downloadButtonState.value == ButtonState.Inactive)
            R.string.please_choose_download
        else
            R.string.please_choose_another_download

    fun downloadButtonClicked() {
        when (downloadButtonState.value) {
            ButtonState.Inactive -> showInfo()
            ButtonState.Active -> download()
            ButtonState.Completed -> showInfo()
            else -> {
            }
        }
    }

    private fun showInfo() {
        _showInfo.value = true
        _showInfo.value = false
    }
}