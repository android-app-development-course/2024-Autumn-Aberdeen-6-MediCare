package com.appdev.medicare.model

data class RegisterRequest(
    val username: String,
    val passwordHash: String
)

data class LoginRequest(
    val username: String,
    val passwordHash: String
)

data class AddMedicationRequest(
    val medicationName: String,
    val patientName: String,
    val dosage: String,
    val remainingAmount: Int,
    val frequency: String,
    val weekMode: String,
    val reminderType: String,
    val expirationDate: String,
    val dateList: List<String>,
    val timeList: List<String>
)

data class GetMedicationTimesRequest(
    val medicationId: Int,
    val date: String
)

data class GetMedicationRecordsRequest(
    val date: String
)

data class GetMedicationInfoRequest(
    val medicationId: Int
)

data class GetAllOnDateRequest(
    val date: String
)

data class DeleteMedicationRecordRequest(
    val dare: String,
    val medicationId: Int
)