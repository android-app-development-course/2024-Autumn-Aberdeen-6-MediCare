package com.appdev.medicare.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "medication")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "medication_name") val medicationName: String,
    @ColumnInfo(name = "patient_name") val patientName: String,
    @ColumnInfo(name = "dosage") val dosage: String,
    @ColumnInfo(name = "remaining_amount") val remainingAmount: Int,
    @ColumnInfo(name = "frequency") val frequency: String,
    @ColumnInfo(name = "week_mode") val weekMode: String,
    @ColumnInfo(name = "reminder_type") val reminderType: String,
    @ColumnInfo(name = "expiration_date") val expirationDate: String,
    @ColumnInfo(name = "uuid") val uuid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "sync_status") val syncStatus: String = "created"
)