package com.appdev.medicare.api

import com.appdev.medicare.model.AddMedicationRequest
import com.appdev.medicare.model.ApiResponse
import com.appdev.medicare.model.DeleteMedicationRecordRequest
import com.appdev.medicare.model.GetAllOnDateRequest
import com.appdev.medicare.model.GetMedicationInfoRequest
import com.appdev.medicare.model.GetMedicationRecordsRequest
import com.appdev.medicare.model.GetMedicationTimesRequest
import com.appdev.medicare.model.InsertCalendarMedicationDataRequest
import com.appdev.medicare.model.InsertMedicationDataRequest
import com.appdev.medicare.model.InsertMedicationTimeDataRequest
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

    @GET("getMedicationData")
    fun getMedicationData(): Call<ApiResponse>

    @GET("getCalendarMedicationData")
    fun getCalendarMedicationData(): Call<ApiResponse>

    @GET("getMedicationTimeData")
    fun getMedicationTimeData(): Call<ApiResponse>

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
}