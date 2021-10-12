package com.udacity.loadapp

import android.app.DownloadManager.*
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.udacity.loadapp.button.ButtonState
import com.udacity.loadapp.button.LoadingButton

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

@BindingAdapter("isCustomUrlSelected")
fun EditText.setState(isCustomUrlSelected: Boolean) {
    if (isCustomUrlSelected) {
        visibility = View.VISIBLE
        isEnabled = true
    } else {
        visibility = View.INVISIBLE
        isEnabled = false
    }
}
