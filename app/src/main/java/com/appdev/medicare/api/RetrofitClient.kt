package com.appdev.medicare.api

import android.content.Context
import android.content.SharedPreferences
import com.appdev.medicare.model.JsonValue
import com.appdev.medicare.model.JsonValueAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.jvm.Throws

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5000/"    // 模拟器使用 10.0.2.2 代指运行模拟器的主机（代宿主机）
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("MediCare", Context.MODE_PRIVATE)
    }

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(JsonValue::class.java, JsonValueAdapter())
        .create()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val token = sharedPreferences.getString("login_token", "")
                val originalRequest: Request = chain.request()
                val requestWithHeaders: Request = originalRequest.newBuilder()
                    .header("Authorization", token!!)
                    .build()
                return chain.proceed(requestWithHeaders)
            }
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)

    fun getGson(): Gson { return gson }
}