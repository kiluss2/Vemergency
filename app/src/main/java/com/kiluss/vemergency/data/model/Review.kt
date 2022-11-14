package com.kiluss.vemergency.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@kotlinx.serialization.Serializable
data class Review(
    var rating: Double? = null,
    var comment: String? = null,
) : Parcelable
