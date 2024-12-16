package com.appdev.medicare.api

import com.appdev.medicare.model.AddMedicationRequest
import com.appdev.medicare.model.ApiResponse
import com.appdev.medicare.model.DeleteMedicationRecordRequest
import com.appdev.medicare.model.GetAllOnDateRequest
import com.appdev.medicare.model.GetMedicationInfoRequest
import com.appdev.medicare.model.GetMedicationRecordsRequest
import com.appdev.medicare.model.LoginRequest
import com.appdev.medicare.model.RegisterRequest
import com.appdev.medicare.model.GetMedicationTimesRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("register")
    fun register(@Body body: RegisterRequest): Call<ApiResponse>

    @POST("login")
    fun login(@Body body: LoginRequest): Call<ApiResponse>

    @GET("checkToken")
    fun checkToken(): Call<ApiResponse>

    @POST("addMedication")
    fun addMedication(@Body body: AddMedicationRequest): Call<ApiResponse>

    @GET("getMedicationTimes")
    fun getMedicationTimes(@Body body: GetMedicationTimesRequest): Call<ApiResponse>

    @GET("getMedicationRecords")
    fun getMedicationRecords(@Body body: GetMedicationRecordsRequest): Call<ApiResponse>

    @GET("getMedicationInfo")
    fun getMedicationInfo(@Body body: GetMedicationInfoRequest): Call<ApiResponse>

    @GET("getAllOnDate")
    fun getAllOnDate(@Body body: GetAllOnDateRequest): Call<ApiResponse>

    @DELETE("deleteMedicationRecord")
    fun deleteMedicationRecord(@Body body: DeleteMedicationRecordRequest): Call<ApiResponse>
}