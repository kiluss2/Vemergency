package com.kiluss.vemergency.data.model

data class User(
    var email: String? = null,
    var fullName: String? = null,
    var birthday: String? = null,
    var address: String? = null,
    var phone: String? = null,
    var reviews: String? = null,
    var shop: Shop? = null,
    var isShopCreated: Boolean = false
)
