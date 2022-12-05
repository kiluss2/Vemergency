package com.kiluss.vemergency.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Created by sonlv on 11/1/2022
 */
@Serializable
@Parcelize
data class Transaction(
    var id: String? = null,
    var userId: String? = null,
    var userFullName: String? = null,
    var userPhone: String? = null,
    var userImage: String? = null,
    var userAddress: String? = null,
    var userLocation: LatLng? = null,
    var userFcmToken: String? = null,
    var shopId: String? = null,
    var shopName: String? = null,
    var shopPhone: String? = null,
    var shopImage: String? = null,
    var service: String? = null,
    var startTime: Double? = null,
    var endTime: Double? = null,
    var content: String? = null,
    var shopAddress: String? = null,
    var distance: Double? = null,
    var duration: Double? = null,
    var shopLocation: LatLng? = null,
    var review: Review? = null
) : Parcelable
