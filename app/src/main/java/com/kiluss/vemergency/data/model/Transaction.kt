package com.kiluss.vemergency.data.model

/**
 * Created by sonlv on 11/1/2022
 */
data class Transaction(
    internal val userUid: String? = null,
    internal val shopUid: String? = null,
    internal val service: String? = null,
    internal val time: Double? = null,
    internal val waiting: Boolean = true,
    internal val content: String? = null,
    internal val review: Review? = null
)
