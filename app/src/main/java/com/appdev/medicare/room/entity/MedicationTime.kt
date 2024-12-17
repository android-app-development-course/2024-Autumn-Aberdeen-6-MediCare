package com.appdev.medicare.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "medication_id") val medicationId: Int,
    @ColumnInfo(name = "date_id") val dateId: Int,
    @ColumnInfo(name = "time") val time: String,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "uuid") val uuid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "sync_status") val syncStatus: String = "created"
)