package com.appdev.medicare.api

import com.appdev.medicare.model.JsonValue
import com.appdev.medicare.model.JsonValueAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5000/"    // 模拟器使用 10.0.2.2 代指运行模拟器的主机（代宿主机）
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(JsonValue::class.java, JsonValueAdapter())
        .create()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)

    fun getGson(): Gson { return gson }
}