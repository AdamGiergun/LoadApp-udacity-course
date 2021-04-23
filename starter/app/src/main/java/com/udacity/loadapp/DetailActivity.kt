package com.udacity.loadapp

import android.app.NotificationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.loadapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityDetailBinding.inflate(layoutInflater).run {
            setContentView(root)
            toolbar.title = getString(R.string.download_details)
            setSupportActionBar(toolbar)
            val download: Download? = intent.getParcelableExtra(Download.EXTRA_NAME)
            download?.let { contentDetail.download = it }
        }

        val notificationId = intent.getIntExtra(LoadAppNotification.EXTRA_ID, 0)
        val notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            ) as NotificationManager
        if (notificationId > 0) notificationManager.cancel(notificationId)
    }
}