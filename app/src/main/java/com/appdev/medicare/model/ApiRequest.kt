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
    val medication_name: String,
    val patient_name: String,
    val dosage: String,
    val remaining_amount: Int,
    val frequency: String,
    val week_mode: String,
    val reminder_type: String,
    val expiration_date: String,
    val date_list: List<String>,
    val time_list: List<String>
)
