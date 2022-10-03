package com.kiluss.vemergency.data.model

import com.google.android.gms.maps.model.LatLng

data class Shop(
    var name: String? = null,
    var address: String? = null,
    var phone: String? = null,
    var openTime: String? = null,
    var website: String? = null,
    var avgStar: Float? = null,
    var review: ArrayList<Review>? = null,
    var user: String? = null,
    var location: LatLng? = null
)
