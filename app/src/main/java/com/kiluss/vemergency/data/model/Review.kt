package com.kiluss.vemergency.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@kotlinx.serialization.Serializable
data class Review(
    var star: Float? = null,
    var comment: String? = null,
    var user: String? = null
) : Parcelable
