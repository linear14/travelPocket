package com.dongldh.travelpocket

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RetrofitManager {
    @GET("/query/{fullcode}")
    fun getRateList(@Path ("fullcode") fullcode: String): Call<Map<String, Any>>
}