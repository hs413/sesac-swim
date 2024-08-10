package com.example.health.api

import android.content.Context
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = ""

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun sendDataToServer(dataList: List<SendData>) {
        dataList.forEach { data ->
            api.sendData(data).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    println(response)
                    return
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    return
                }
            })
        }
    }
}