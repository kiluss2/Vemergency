package com.kiluss.vemergency.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
@Parcelize data class Shop(
    var uid: String? = null,
    var name: String? = null,
    internal var owner: String? = null,
    internal var address: String? = null,
    internal var phone: String? = null,
    internal var openTime: String? = null,
    internal var website: String? = null,
    internal var imageUrl: String? = null,
    internal var service: String? = null,
    internal var rating: Double? = null,
    internal var reviewCount: Long? = null,
    internal var location: @RawValue HashMap<String, Any>? = null,
    internal var pendingApprove: Boolean? = null,
    internal var created: Boolean? = null,
    internal var lastModifiedTime: Double? = null,
    internal var fcmToken: String? = null,
    internal var isReady: Boolean = false,
) : Parcelable
