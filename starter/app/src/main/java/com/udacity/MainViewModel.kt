package com.udacity

import android.app.*
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
                val notificationManager = application.getSystemService(NotificationManager::class.java)
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
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            context?.let {
                if (id == downloadID) _downloadButtonState.value = ButtonState.Completed

                val notificationIntent = Intent(context, DetailActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent: PendingIntent =
                    PendingIntent.getActivity(context, 0, notificationIntent, 0)

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_cloud_download)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_description))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                with(NotificationManagerCompat.from(context)) {
                    notify(NOTIFICATION_ID, builder.build())
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
    private val downloadDir =
        ContextCompat.getExternalFilesDirs(application, Environment.DIRECTORY_DOWNLOADS)[0]

    private fun download() {
        _downloadButtonState.value = ButtonState.Loading
        val fileName = uri.pathSegments[uri.pathSegments.size - 3]
        val file = File("${downloadDir.path}/$fileName")
        val app = getApplication<Application>()

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

    companion object {
        private const val CHANNEL_ID = "loadAppChannel"
        private const val CHANNEL_NAME = "LoadApp"
        private const val NOTIFICATION_ID = 1
    }
}