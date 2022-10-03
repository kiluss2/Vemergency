package com.kiluss.vemergency.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LatLng(var latitude: Double? = null, var longitude: Double? = null) : Parcelable
