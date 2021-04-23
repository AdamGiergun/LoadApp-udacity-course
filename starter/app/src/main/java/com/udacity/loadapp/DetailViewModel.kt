package com.udacity.loadapp

import android.app.Application
import android.app.NotificationManager
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DetailViewModel(application: Application, intent: Intent) : AndroidViewModel(application) {

    var download: Download? = intent.getParcelableExtra(Download.EXTRA_NAME)

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

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(application, intent) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}