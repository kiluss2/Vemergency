package com.kiluss.vemergency.network.api

import com.kiluss.bookrate.data.model.*
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ImageService {

    //account
    @Headers("Accept: text/plain")
    @POST("1/upload")
    fun upload(
        @Body params: RequestBody
    ): Call<Account>

    @POST("User/MyBook")
    fun postMyBook(
        @Body params: RequestBody
    ): Call<MyBookState>
}
