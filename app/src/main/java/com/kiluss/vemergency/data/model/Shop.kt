package com.kiluss.vemergency.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Shop(
    var name: String? = null,
    var owner: String? = null,
    var address: String? = null,
    var phone: String? = null,
    var openTime: String? = null,
    var website: String? = null,
    var pricePerKilometer: Double? = null,
    var avgStar: Float? = null,
    var review: ArrayList<Review>? = null,
    var location: LatLng? = null
) : Parcelable
