package com.kiluss.vemergency.data.model

data class User(
    internal var uid: String? = null,
    internal var email: String? = null,
    internal var fullName: String? = null,
    internal var birthday: String? = null,
    internal var address: String? = null,
    internal var phone: String? = null,
    internal var reviews: String? = null,
    internal var imageUrl: String? = null
)
