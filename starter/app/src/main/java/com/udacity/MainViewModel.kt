package com.udacity

import android.app.*
import android.app.DownloadManager.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.RadioGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var notificationId = 1

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).run {
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                description = application.getString(R.string.channel_for_load_app)
                val notificationManager =
                    application.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    private val _downloadButtonState = MutableLiveData<ButtonState>().apply {
        value = ButtonState.Inactive
    }
    val downloadButtonState: LiveData<ButtonState>
        get() = _downloadButtonState

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.run {
                var fileLocalUri = ""
                var downloadStatus = 0
                intent?.getLongExtra(EXTRA_DOWNLOAD_ID, -1)?.let { id ->
                    if (id == downloadID) _downloadButtonState.value = ButtonState.Completed

                    val downloadManager =
                        getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val query = Query().setFilterById(id)
                    downloadManager.query(query).use { cursor ->
                        if (cursor.moveToFirst()) {
                            var index = cursor.getColumnIndex(COLUMN_LOCAL_URI)
                            fileLocalUri = cursor.getString(index)
                            index = cursor.getColumnIndex(COLUMN_STATUS)
                            downloadStatus = cursor.getInt(index)
                        }
                    }
                }

                val downloadName = when {
                    fileLocalUri.contains("glide") -> R.string.glide_library
                    fileLocalUri.contains("nd940") -> R.string.loadapp_repository
                    fileLocalUri.contains("retrofit") -> R.string.retrofit_client
                    else -> 0
                }
                val notificationIntent = Intent(this, DetailActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    val download = Download(downloadName, downloadStatus, fileLocalUri)
                    putExtra("download", download)
                    putExtra("notification_id", notificationId)
                }
                val pendingIntent = PendingIntent.getActivity(this, notificationId, notificationIntent, 0)

                NotificationCompat.Builder(this, CHANNEL_ID).apply {
                    setSmallIcon(R.drawable.ic_assistant_black_24dp)
                    setContentTitle(getString(R.string.notification_title))
                    setContentText(getString(R.string.notification_description))
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setAutoCancel(true)
                    addAction(
                        R.drawable.ic_assistant_black_24dp,
                        getString(R.string.details),
                        pendingIntent
                    )

                    with(NotificationManagerCompat.from(this@run)) {
                        notify(notificationId, build())
                    }
                    notificationId++
                }
            }
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

        val fileName = uri.pathSegments[uri.pathSegments.size - 3]
        val file = File("${getDownloadDir(app)}/$fileName.zip")

        val request = Request(uri).apply {
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
        downloadID = downloadManager?.enqueue(request) ?: -1
    }

    private fun getDownloadDir(app: Application) = app.run {
        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path ?: filesDir.path
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

    companion object {
        private const val CHANNEL_ID = "loadAppChannel"
        private const val CHANNEL_NAME = "LoadApp"
    }
}