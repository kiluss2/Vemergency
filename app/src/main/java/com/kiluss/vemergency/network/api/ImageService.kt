package com.kiluss.vemergency.network.api

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ImageService {

    //account
    @Headers("Accept: text/plain")
    @POST("1/upload")
    fun upload(
        @Body params: RequestBody
    ): Call<String>
}
