package com.udacity.loadapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.udacity.loadapp.viewmodels.DetailViewModel
import com.udacity.loadapp.viewmodels.DetailViewModelFactory
import com.udacity.loadapp.R
import com.udacity.loadapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private val detailViewModel by viewModels<DetailViewModel> {
        DetailViewModelFactory(
            application,
            intent
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityDetailBinding.inflate(layoutInflater).run {
            setContentView(root)
            toolbar.title = getString(R.string.download_details)
            setSupportActionBar(toolbar)
            detailViewModel.download?.let {
                contentDetail.download = it
            }
        }
    }

    fun onReturnClick(view: View) {
        Intent(this, MainActivity::class.java).run {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
    }

    fun onOpenClick(view: View) {
        detailViewModel.downloadLocalUri?.let { localUri ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val mimeTypeForUri = getMimeTypeForUri(localUri)
                setDataAndType(localUri, mimeTypeForUri)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            try {
                startActivity(
                    Intent.createChooser(
                        intent,
                        getString(R.string.choose_an_app_to_open_with)
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.error_opening_file), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun getMimeTypeForUri(localUri: Uri): String =
        DocumentFile.fromSingleUri(this, localUri)?.type ?: getString(R.string.zip_mime_type)
}