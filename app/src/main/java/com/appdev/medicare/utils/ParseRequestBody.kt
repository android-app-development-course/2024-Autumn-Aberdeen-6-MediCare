package com.appdev.medicare.utils

import android.util.Log
import com.appdev.medicare.api.RetrofitClient
import com.appdev.medicare.model.ApiResponse
import okhttp3.ResponseBody

fun parseRequestBody(body: ResponseBody?): ApiResponse {
    val gson = RetrofitClient.getGson()
    val json = body?.string()

    try {
        val apiResponse = gson.fromJson(json, ApiResponse::class.java)
        return apiResponse
    } catch (e: Exception) {
        Log.e("parseRequestBody", "Gson parsing error: ${e.message}")
        throw e
    }
}