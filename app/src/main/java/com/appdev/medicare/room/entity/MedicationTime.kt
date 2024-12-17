package com.appdev.medicare.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_time",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CalendarMedication::class,
            parentColumns = ["id"],
            childColumns = ["date_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicationTime(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "medication_id") val medicationId: Int,
    @ColumnInfo(name = "date_id") val dateId: Int,
    @ColumnInfo(name = "time") val time: String,
    @ColumnInfo(name = "status") val status: Int
)