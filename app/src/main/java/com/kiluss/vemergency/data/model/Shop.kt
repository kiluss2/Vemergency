package com.kiluss.vemergency.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize data class Shop(
    internal var uid: String? = null,
    internal var name: String? = null,
    internal var owner: String? = null,
    internal var address: String? = null,
    internal var phone: String? = null,
    internal var openTime: String? = null,
    internal var website: String? = null,
    internal var pricePerKilometer: Double? = null,
    internal var avgStar: Float? = null,
    internal var location: @RawValue HashMap<String, Any>? = null,
    internal var isPendingApprove: Boolean? = null,
    internal var isCreated: Boolean? = null
) : Parcelable
