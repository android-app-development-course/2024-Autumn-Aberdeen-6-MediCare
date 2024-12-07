package com.appdev.medicare.api

import com.appdev.medicare.model.ApiResponse
import com.appdev.medicare.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("register")
    fun register(@Body body: RegisterRequest): Call<ApiResponse>
}