package com.kiluss.vemergency.network.api

import com.google.gson.JsonObject
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface ImageService {

    @POST("1/upload")
    fun upload(
        @Body params: RequestBody
    ): Call<JsonObject>
}
