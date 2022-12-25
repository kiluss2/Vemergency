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
    fun sendNotiEmergency(
        @Body params: RequestBody
    ): Call<String>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("acceptShopNoti")
    fun sendNotiAcceptShop(
        @Body params: RequestBody
    ): Call<String>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("rejectShopNoti")
    fun sendNotiRejectShop(
        @Body params: RequestBody
    ): Call<String>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("createShopNoti")
    fun createShopRequestNoti(): Call<String>
}
