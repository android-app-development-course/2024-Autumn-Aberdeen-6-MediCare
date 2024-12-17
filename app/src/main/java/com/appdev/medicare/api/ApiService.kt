package com.appdev.medicare.api

import com.appdev.medicare.model.AddMedicationRequest
import com.appdev.medicare.model.ApiResponse
import com.appdev.medicare.model.DeleteMedicationRecordRequest
import com.appdev.medicare.model.GetAllOnDateRequest
import com.appdev.medicare.model.GetMedicationInfoRequest
import com.appdev.medicare.model.GetMedicationRecordsRequest
import com.appdev.medicare.model.GetMedicationTimesRequest
import com.appdev.medicare.model.LoginRequest
import com.appdev.medicare.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
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

    @POST("getMedicationTimes")
    fun getMedicationTimes(@Body body: GetMedicationTimesRequest): Call<ApiResponse>

    @POST("getMedicationRecords")
    fun getMedicationRecords(@Body body: GetMedicationRecordsRequest): Call<ApiResponse>

    @POST("getMedicationInfo")
    fun getMedicationInfo(@Body body: GetMedicationInfoRequest): Call<ApiResponse>

    @POST("getAllOnDate")
    fun getAllOnDate(@Body body: GetAllOnDateRequest): Call<ApiResponse>

    @POST("deleteMedicationRecord")
    fun deleteMedicationRecord(@Body body: DeleteMedicationRecordRequest): Call<ApiResponse>
}