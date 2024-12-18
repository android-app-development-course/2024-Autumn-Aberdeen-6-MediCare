package com.appdev.medicare.model

data class RegisterRequest(
    val username: String,
    val passwordHash: String
)

data class LoginRequest(
    val username: String,
    val passwordHash: String
)

data class InsertMedicationDataRequest(
    val medicationTime: List<Map<String, JsonValue>>
)

data class InsertCalendarMedicationDataRequest(
    val calendarMedication: List<Map<String, JsonValue>>
)

data class InsertMedicationTimeDataRequest(
    val medicationTime: List<Map<String, JsonValue>>
)