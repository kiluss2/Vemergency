package com.kiluss.vemergency.data.model

/**
 * Created by sonlv on 11/1/2022
 */
data class Transaction(
    var userUid: String? = null,
    var shopUid: String? = null,
    var service: String? = null,
    var startTime: Double? = null,
    var endTime: Double? = null,
    var content: String? = null,
    var userLocation: LatLng? = null,
    var shopLocation: LatLng? = null,
    var review: Review? = null
)
