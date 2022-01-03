package com.udacity.loadapp.viewmodels

import android.app.Application
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.udacity.loadapp.Download
import com.udacity.loadapp.R
import com.udacity.loadapp.notification.LoadAppNotification

class DetailViewModel(application: Application, intent: Intent) : AndroidViewModel(application) {

    val download: Download? = intent.getParcelableExtra(Download.EXTRA_NAME)
    val downloadLocalUri: Uri? = download?.let { Uri.parse(it.localUriString) }
    val downloadMimeType: Int? = download?.let { if (it.details ==  R.string.custom_download) R.string.any_mime_type else R.string.zip_mime_type }

    init {
        val notificationId = intent.getIntExtra(LoadAppNotification.EXTRA_ID, 0)
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