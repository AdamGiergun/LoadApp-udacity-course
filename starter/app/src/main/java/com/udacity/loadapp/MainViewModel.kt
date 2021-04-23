package com.udacity.loadapp

import android.app.*
import android.app.DownloadManager.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.widget.RadioGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.abs
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var notificationId = abs(Random.nextInt())

    init {
        LoadAppNotificationChannel.create(application)
    }

    private val _downloadButtonState = MutableLiveData<ButtonState>().apply {
        value = ButtonState.Inactive
    }
    val downloadButtonState: LiveData<ButtonState>
        get() = _downloadButtonState

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.run {
                var downloadStatus = STATUS_PENDING
                var downloadTitle = ""
                intent?.getLongExtra(EXTRA_DOWNLOAD_ID, -1)?.let { id ->
                    if (id == downloadID) _downloadButtonState.value = ButtonState.Completed

                    val downloadManager =
                        getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val query = Query().setFilterById(id)
                    downloadManager.query(query).use { cursor ->
                        if (cursor.moveToFirst()) {
                            var index = cursor.getColumnIndex(COLUMN_STATUS)
                            downloadStatus = cursor.getInt(index)
                            index = cursor.getColumnIndex(COLUMN_TITLE)
                            downloadTitle = cursor.getString(index)
                        }
                    }
                }

                val downloadDetails = when {
                    downloadTitle.contains("glide") -> R.string.glide_library
                    downloadTitle.contains("nd940") -> R.string.loadapp_repository
                    downloadTitle.contains("retrofit") -> R.string.retrofit_client
                    else -> R.string.unknown
                }
                val notificationIntent = Intent(this, DetailActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    val download = Download(downloadTitle, downloadDetails, downloadStatus)
                    putExtra("download", download)
                    putExtra("notification_id", notificationId)
                }
                val pendingIntent =
                    PendingIntent.getActivity(this, notificationId, notificationIntent, 0)

                NotificationCompat.Builder(this, LoadAppNotificationChannel.CHANNEL_ID).apply {
                    setSmallIcon(R.drawable.ic_assistant_black_24dp)
                    setContentTitle(getString(R.string.notification_title))
                    setContentText(getString(R.string.notification_description))
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setAutoCancel(true)
                    addAction(
                        R.drawable.ic_assistant_black_24dp,
                        getString(R.string.details_cap),
                        pendingIntent
                    )

                    with(NotificationManagerCompat.from(this@run)) {
                        notify(notificationId, build())
                    }
                    notificationId++
                }
            }
            context?.unregisterReceiver(this)
        }
    }

    private var downloadOptionId = -1
    private lateinit var uri: Uri

    fun RadioGroup.setDownloadOptionId(id: Int) {
        if (downloadOptionId != id) {
            _downloadButtonState.value = ButtonState.Active
            downloadOptionId = id
            uri = Uri.parse(
                when (id) {
                    R.id.glide_radio -> "https://github.com/bumptech/glide/archive/master.zip"
                    R.id.loadapp_radio -> "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
                    R.id.retrofit_radio -> "https://github.com/square/retrofit/archive/master.zip"
                    else -> ""
                }
            )
        }
    }

    private var downloadID: Long = 0

    private fun download() {
        _downloadButtonState.value = ButtonState.Loading
        val app = getApplication<Application>()

        val title = uri.pathSegments[uri.pathSegments.size - 3]

        val request = Request(uri).apply {
            setTitle(title)
            setDescription(app.getString(R.string.app_description).replace("files", title))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setRequiresCharging(false)
            }
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        app.registerReceiver(receiver, IntentFilter(ACTION_DOWNLOAD_COMPLETE))
        val downloadManager = ContextCompat.getSystemService(app, DownloadManager::class.java)
        downloadID = downloadManager?.enqueue(request) ?: -1
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