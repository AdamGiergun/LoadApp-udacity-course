package com.udacity.loadapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Download(val title: String, val details: Int, val status: Int) : Parcelable