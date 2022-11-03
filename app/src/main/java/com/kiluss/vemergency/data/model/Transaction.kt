package com.kiluss.vemergency.data.model

/**
 * Created by sonlv on 11/1/2022
 */
data class Transaction(
    internal var userUid: String? = null,
    internal var shopUid: String? = null,
    internal var service: String? = null,
    internal var startTime: Double? = null,
    internal var endTime: Double? = null,
    internal var content: String? = null,
    internal var userLocation: LatLng? = null,
    internal var shopLocation: LatLng? = null,
    internal var review: Review? = null
)
