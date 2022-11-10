package com.kiluss.vemergency.network.api

import com.google.gson.JsonObject
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @POST("1/upload")
    fun uploadPhoto(
        @Body params: RequestBody
    ): Call<JsonObject>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("noti")
    fun sendNoti(
        @Body params: RequestBody
    ): Call<JsonObject>
}
