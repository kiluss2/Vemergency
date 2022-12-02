package com.kiluss.vemergency.data.model

data class User(
    var id: String? = null,
    var email: String? = null,
    var fullName: String? = null,
    var birthday: String? = null,
    var address: String? = null,
    var phone: String? = null,
    var imageUrl: String? = null,
    var lastModifiedTime: Double? = null,
    var inEmergency: Boolean? = null,
    var fcmToken: String? = null
)
