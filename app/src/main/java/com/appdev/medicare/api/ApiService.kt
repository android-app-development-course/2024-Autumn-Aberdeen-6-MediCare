package com.appdev.medicare.api

import com.appdev.medicare.model.ApiResponse
import com.appdev.medicare.model.LoginRequest
import com.appdev.medicare.model.RegisterRequest
import com.appdev.medicare.model.AddMedicationRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

interface ApiService {
    @POST("register")
    fun register(@Body body: RegisterRequest): Call<ApiResponse>

    @POST("login")
    fun login(@Body body: LoginRequest): Call<ApiResponse>

    @GET("check_token")
    fun checkToken(): Call<ApiResponse>

    @POST("add_medication")
    fun addMedication(@Body body: AddMedicationRequest): Call<ApiResponse>

    @GET("check_time")
    fun getMedicationTimes(
        @Query("medication_id") medicationId: Int,
        @Query("date") date: Date
    ): Call<ApiResponse>

    @GET("check_date")
    fun getMedicationRecords(
        @Query("date") date: Date
    ): Call<ApiResponse>

    @GET("check_medic")
    fun getMedicationInfo(
        @Query("medication_id") medicationId: Int
    ): Call<ApiResponse>

    @GET("check_all")
    fun getAll(
        @Query("date") date: String
    ): Call<ApiResponse>

    // 删除用药记录
    @DELETE("delete_record")
    fun deleteMedicationRecord(
        @Query("date") date: String,
        @Query("medication_id") medicationId: Int
    ): Call<ApiResponse>
}