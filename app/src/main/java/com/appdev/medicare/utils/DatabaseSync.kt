package com.appdev.medicare.utils

import android.content.Context
import android.util.Log
import com.appdev.medicare.api.RetrofitClient
import com.appdev.medicare.model.JsonValue
import com.appdev.medicare.room.DatabaseBuilder
import com.appdev.medicare.room.entity.CalendarMedication
import com.appdev.medicare.room.entity.Medication
import com.appdev.medicare.room.entity.MedicationTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface DatabaseSync {
    suspend fun getAllDataFromServer() {
        getMedicationDataFromServer()
        getCalendarMedicationDataFromServer()
        getMedicationTimeDataFromServer()
    }

    suspend fun getMedicationDataFromServer() {
        val appDatabase = DatabaseBuilder.getInstance()
        val response = withContext(Dispatchers.IO) {
            RetrofitClient.api.getMedicationData().execute()
        }

        if (response.isSuccessful) {
            val data = response.body()!!.data as JsonValue.JsonObject
            val medications = data.value["medication"] as JsonValue.JsonList

            for (item in medications) {
                val value = (item as JsonValue.JsonObject).value
                val medication = Medication(
                    medicationName = value["medicationName"].toString(),
                    patientName = value["patientName"].toString(),
                    dosage = value["dosage"].toString(),
                    remainingAmount = value["remainingAmount"].toString(),
                    frequency = value["frequency"].toString(),
                    weekMode = value["weekMode"].toString(),
                    reminderType = value["reminderType"].toString(),
                    expirationDate = value["expirationDate"].toString(),
                    uuid = value["uuid"].toString(),
                    syncStatus = "synced"
                )
                appDatabase.medicationDao().insertOne(medication)
            }
        } else {
            val data = parseRequestBody(response.errorBody())
            Log.e(
                "DatabaseSync",
                "Failed to get medication table data from server, code: ${response.code()}, reason: ${data.error?.code} - ${data.error?.description}"
            )
        }
    }

    suspend fun getCalendarMedicationDataFromServer() {
        val appDatabase = DatabaseBuilder.getInstance()
        val response = withContext(Dispatchers.IO) {
            RetrofitClient.api.getCalendarMedicationData().execute()
        }

        if (response.isSuccessful) {
            val data = response.body()!!.data as JsonValue.JsonObject
            val calendarMedications = data.value["calendarMedication"] as JsonValue.JsonList

            for (item in calendarMedications) {
                val value = (item as JsonValue.JsonObject).value
                val medicationId = appDatabase.medicationDao().findIdByUuid(value["medicationUuid"].toString())
                val calendarMedication = CalendarMedication(
                    medicationId = medicationId,
                    date = value["date"].toString(),
                    uuid = value["uuid"].toString(),
                    syncStatus = "synced"
                )
                appDatabase.calendarMedicationDao().insertOne(calendarMedication)
            }
        } else {
            val data = parseRequestBody(response.errorBody())
            Log.e(
                "DatabaseSync",
                "Failed to get calendar_medication table data from server, code: ${response.code()}, reason: ${data.error?.code} - ${data.error?.description}"
            )
        }
    }

    suspend fun getMedicationTimeDataFromServer() {
        val appDatabase = DatabaseBuilder.getInstance()
        val response = withContext(Dispatchers.IO) {
            RetrofitClient.api.getMedicationTimeData().execute()
        }

        if (response.isSuccessful) {
            val data = response.body()!!.data as JsonValue.JsonObject
            val medicationTimes = data.value["medicationTime"] as JsonValue.JsonList
            for (item in medicationTimes) {
                val value = (item as JsonValue.JsonObject).value
                val medicationId = appDatabase.medicationDao().findIdByUuid(value["medicationId"].toString())
                val dateId = appDatabase.medicationTimeDao().fundIdByUuid(value["dateUuid"].toString())
                val medicationTime = MedicationTime(
                    medicationId = medicationId,
                    dateId = dateId,
                    time = value["time"].toString(),
                    status = value["status"].toString().toInt(),
                    uuid = value["uuid"].toString(),
                    syncStatus = "synced"
                )
                appDatabase.medicationTimeDao().insertOne(medicationTime)
            }
        } else {
            val data = parseRequestBody(response.errorBody())
            Log.e(
                "DatabaseSync",
                "Failed to get medication_time data from server, code: ${response.code()}, reason: ${data.error?.code} - ${data.error?.description}"
            )
        }
    }

    suspend fun checkUpdate(context: Context) {
        val preferences = context.getSharedPreferences("MediCare", Context.MODE_PRIVATE)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHH:mm:ss")
        val clientLastUpdate = LocalDateTime.parse(preferences.getString("dataLastUpdate", "1970-01-01 00:00:00") as String, formatter)

        val response = withContext(Dispatchers.IO) {
            RetrofitClient.api.getLastUpdateTime().execute()
        }

        if (response.isSuccessful) {
            val data = (response.body()!!.data as JsonValue.JsonObject).value
            val serverLastUpdate = LocalDateTime.parse(data["lastUpdateTime"].toString(), formatter)

            if (serverLastUpdate > clientLastUpdate) {
                // 服务器数据比本地数据新
                // TODO: 将服务器数据更新至本地
            } else if (serverLastUpdate < clientLastUpdate) {
                // 本地数据比服务器数据新
                // TODO: 将本地数据更新至服务器
            }
        }
    }
}