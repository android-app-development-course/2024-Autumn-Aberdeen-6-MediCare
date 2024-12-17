package com.appdev.medicare.utils

import android.content.Context
import android.util.Log
import com.appdev.medicare.api.RetrofitClient
import com.appdev.medicare.model.InsertCalendarMedicationDataRequest
import com.appdev.medicare.model.InsertMedicationDataRequest
import com.appdev.medicare.model.InsertMedicationTimeDataRequest
import com.appdev.medicare.model.JsonValue
import com.appdev.medicare.room.AppDatabase
import com.appdev.medicare.room.DatabaseBuilder
import com.appdev.medicare.room.entity.CalendarMedication
import com.appdev.medicare.room.entity.Medication
import com.appdev.medicare.room.entity.MedicationTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface DatabaseSync {
    suspend fun overwriteAllDataFromServer() {
        val appDatabase = DatabaseBuilder.getInstance()
        // 删除本地数据并重新从服务器获取
        appDatabase.clearAllTables()
        getMedicationDataFromServer(appDatabase)
        getCalendarMedicationDataFromServer(appDatabase)
        getMedicationTimeDataFromServer(appDatabase)
    }

    suspend fun overwriteAllDataToServer() {
        // 删除服务器数据并重新从本地上传
        val appDatabase = DatabaseBuilder.getInstance()
        val response = withContext(Dispatchers.IO) {
            RetrofitClient.api.clearData().execute()
        }

        if (response.isSuccessful) {
            Log.i("DatabaseSync", "Successfully cleared data in server.")
            insertMedicationDataToServer(appDatabase)
            insertCalendarMedicationDataToServer(appDatabase)
            insertMedicationTimeDataToServer(appDatabase)
        }
    }

    private suspend fun getMedicationDataFromServer(appDatabase: AppDatabase) {
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

    private suspend fun getCalendarMedicationDataFromServer(appDatabase: AppDatabase) {
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

    private suspend fun getMedicationTimeDataFromServer(appDatabase: AppDatabase) {
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

    private suspend fun insertMedicationDataToServer(appDatabase: AppDatabase) {
        val medications = appDatabase.medicationDao().getAll()
        val medicationList: MutableList<Map<String, JsonValue>> = mutableListOf()

        for (item in medications) {
            val medicationData = mapOf<String, JsonValue>(
                "uuid" to JsonValue.JsonString(item.uuid),
                "medicationName" to JsonValue.JsonString(item.medicationName),
                "patientName" to JsonValue.JsonString(item.patientName),
                "dosage" to JsonValue.JsonString(item.dosage),
                "remainingAmount" to JsonValue.JsonString(item.remainingAmount),
                "frequency" to JsonValue.JsonString(item.frequency),
                "weekMode" to JsonValue.JsonString(item.weekMode),
                "reminderType" to JsonValue.JsonString(item.reminderType),
                "expirationDate" to JsonValue.JsonString(item.expirationDate)
            )
            medicationList.add(medicationData)
        }

        val insertMedicationDataRequest = InsertMedicationDataRequest(medicationList)
        val response = withContext(Dispatchers.IO) {
            RetrofitClient.api.insertMedicationData(insertMedicationDataRequest).execute()
        }

        if (response.isSuccessful) {
            Log.i("DatabaseSync", "Successfully inserted data to server.")
        }
    }

    private suspend fun insertCalendarMedicationDataToServer(appDatabase: AppDatabase) {
        val calendarMedications = appDatabase.calendarMedicationDao().getAll()
        val calendarMedicationList: MutableList<Map<String, JsonValue>> = mutableListOf()

        for (item in calendarMedications) {
            val medicationUuid = appDatabase.medicationDao().findUuidById(item.medicationId)
            val calendarMedicationData = mapOf<String, JsonValue>(
                "uuid" to JsonValue.JsonString(item.uuid),
                "medicationUuid" to JsonValue.JsonString(medicationUuid),
                "date" to JsonValue.JsonString(item.date)
            )
            calendarMedicationList.add(calendarMedicationData)
        }

        val insertCalendarMedicationDataRequest = InsertCalendarMedicationDataRequest(calendarMedicationList)
        val response = withContext(Dispatchers.IO) {
            RetrofitClient.api.insertCalendarMedicationData(insertCalendarMedicationDataRequest).execute()
        }

        if (response.isSuccessful) {
            Log.i("DatabaseSync", "Successfully inserted data to server.")
        }
    }

    private suspend fun insertMedicationTimeDataToServer(appDatabase: AppDatabase) {
        val medicationTimes = appDatabase.medicationTimeDao().getAll()
        val medicationTimeList: MutableList<Map<String, JsonValue>> = mutableListOf()

        for (item in medicationTimes) {
            val medicationUuid = appDatabase.medicationDao().findUuidById(item.medicationId)
            val dateUuid = appDatabase.calendarMedicationDao().findUuidById(item.dateId)
            val medicationTimeData = mapOf<String, JsonValue>(
                "uuid" to JsonValue.JsonString(item.uuid),
                "medicationUuid" to JsonValue.JsonString(medicationUuid),
                "dateUuid" to JsonValue.JsonString(dateUuid),
                "status" to JsonValue.JsonNumber(item.status),
                "time" to JsonValue.JsonString(item.time)
            )
            medicationTimeList.add(medicationTimeData)
        }

        val insertMedicationTimeDataRequest = InsertMedicationTimeDataRequest(medicationTimeList)
        val response = withContext(Dispatchers.IO) {
            RetrofitClient.api.insertMedicationTimeData(insertMedicationTimeDataRequest).execute()
        }

        if (response.isSuccessful) {
            Log.i("DatabaseSync", "Successfully inserted data to server.")
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
                overwriteAllDataFromServer()
                val editor = preferences.edit()
                editor.putString("dataLastUpdate", data["lastUpdateTime"].toString())
                editor.apply()
                Log.i("DatabaseSync", "Successfully synced data from server.")
            } else if (serverLastUpdate < clientLastUpdate) {
                // 本地数据比服务器数据新
                overwriteAllDataToServer()
                // TODO: 更新 dataLastUpdatePref
                Log.i("DatabaseSync", "Successfully synced data to server.")
            } else {
                Log.i("DatabaseSync", "All data up to date.")
            }
        }
    }
}