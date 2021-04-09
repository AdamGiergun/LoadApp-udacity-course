package com.udacity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Download(val details: String, val status: Int, val localPath: String): Parcelable