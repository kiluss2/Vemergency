package com.kiluss.vemergency.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Shop(
    var id: String? = null,
    var name: String? = null,
    var owner: String? = null,
    var address: String? = null,
    var phone: String? = null,
    var openTime: String? = null,
    var website: String? = null,
    var imageUrl: String? = null,
    var service: String? = null,
    var rating: Double? = null,
    var reviewCount: Long? = null,
    var location: @RawValue HashMap<String, Any>? = null,
    var pendingApprove: Boolean? = null,
    var created: Boolean? = null,
    var lastModifiedTime: Double? = null,
    var fcmToken: String? = null,
    var ready: Boolean = true,
) : Parcelable
