package com.dongldh.travelpocket

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RetrofitManager {
    @GET("/query/{fromto},{tofrom}")
    fun getRateList(@Path ("fromto") fromto: String, @Path("tofrom") tofrom: String): Call<Map<String, Any>>
}