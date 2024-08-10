package com.example.health.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class SendData (
    val value: Double,
    val stopTime: String,
    val startTime: String,
    val category: String
)

interface ApiService {
    @POST("record/")
    fun sendData(@Body data: SendData): Call<Void>
}