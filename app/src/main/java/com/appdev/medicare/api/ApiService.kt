package com.appdev.medicare.api

import com.appdev.medicare.model.ApiResponse
import com.appdev.medicare.model.InsertCalendarMedicationDataRequest
import com.appdev.medicare.model.InsertMedicationDataRequest
import com.appdev.medicare.model.InsertMedicationTimeDataRequest
import com.appdev.medicare.model.InsertMedicineBoxDataRequest
import com.appdev.medicare.model.LoginRequest
import com.appdev.medicare.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("ping")
    fun ping(): Call<ApiResponse>

    @POST("register")
    fun register(@Body body: RegisterRequest): Call<ApiResponse>

    @POST("login")
    fun login(@Body body: LoginRequest): Call<ApiResponse>

    @GET("checkToken")
    fun checkToken(): Call<ApiResponse>

    @GET("getMedicationData")
    fun getMedicationData(): Call<ApiResponse>

    @GET("getCalendarMedicationData")
    fun getCalendarMedicationData(): Call<ApiResponse>

    @GET("getMedicationTimeData")
    fun getMedicationTimeData(): Call<ApiResponse>

    @GET("getMedicineBoxData")
    fun getMedicineBoxData(): Call<ApiResponse>

    @GET("getLastUpdateTime")
    fun getLastUpdateTime(): Call<ApiResponse>

    @DELETE("clearData")
    fun clearData(): Call<ApiResponse>

    @POST("insertMedicationData")
    fun insertMedicationData(@Body body: InsertMedicationDataRequest): Call<ApiResponse>

    @POST("insertCalendarMedicationData")
    fun insertCalendarMedicationData(@Body body: InsertCalendarMedicationDataRequest): Call<ApiResponse>

    @POST("insertMedicationTimeData")
    fun insertMedicationTimeData(@Body body: InsertMedicationTimeDataRequest): Call<ApiResponse>

    @POST("insertMedicineBoxData")
    fun insertMedicineBoxData(@Body body: InsertMedicineBoxDataRequest): Call<ApiResponse>
}