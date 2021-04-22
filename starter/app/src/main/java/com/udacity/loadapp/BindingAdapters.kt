package com.udacity.loadapp

import android.app.DownloadManager.*
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("buttonState")
fun LoadingButton.setButtonState(buttonState: ButtonState) {
    setState(buttonState)
}

@BindingAdapter("downloadStatus")
fun TextView.setDownloadStateText(downloadStatus: Int) {
    setText(
        when (downloadStatus) {
            STATUS_FAILED -> R.string.failed
            STATUS_PAUSED -> R.string.paused
            STATUS_PENDING -> R.string.pending
            STATUS_RUNNING -> R.string.running
            STATUS_SUCCESSFUL -> R.string.successful
            else -> R.string.unknown
        }
    )
}
