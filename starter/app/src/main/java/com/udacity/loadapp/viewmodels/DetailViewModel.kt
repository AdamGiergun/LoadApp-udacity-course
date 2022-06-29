package com.udacity.loadapp.viewmodels

import android.app.Application
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.udacity.loadapp.Download
import com.udacity.loadapp.R
import com.udacity.loadapp.notification.LoadAppNotification

class DetailViewModel(application: Application, sourceIntent: Intent) :
    AndroidViewModel(application) {

    val download: Download? = sourceIntent.getParcelableExtra(Download.EXTRA_NAME)
    private val downloadLocalUri: Uri? = download?.let { Uri.parse(it.localUriString) }
    private val downloadMimeType: Int =
        download?.let { if (it.details == R.string.custom_download) R.string.any_mime_type else R.string.zip_mime_type }
            ?: R.string.any_mime_type

    private val appContext
        get() = getApplication<Application>().applicationContext

    val intent: Intent
        get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
        } else {
            Intent(Intent.ACTION_VIEW).run {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(
                    downloadLocalUri,
                    downloadMimeType.let { appContext.getString(it) })
                Intent.createChooser(
                    this, appContext.getString(R.string.choose_an_app_to_open_with)
                )
            }
        }

    init {
        val notificationId = sourceIntent.getIntExtra(LoadAppNotification.EXTRA_ID, 0)
        if (notificationId > 0) {
            (ContextCompat.getSystemService(
                application,
                NotificationManager::class.java
            ) as NotificationManager).run {
                cancel(notificationId)
            }
        }
    }
}

class DetailViewModelFactory(private val application: Application, private val intent: Intent) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(application, intent) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}