package com.appdev.medicare.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendar_medication",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["medication_id"],
            childColumns = ["medication_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CalendarMedication(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "medication_id") val medicationId: Int,
    @ColumnInfo(name = "date") val date: String
)