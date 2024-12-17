package com.appdev.medicare.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "medicine_box",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicineBox(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "box_name") val boxName: String,
    @ColumnInfo(name = "box_type") val boxType: String,
    @ColumnInfo(name = "applicable_people") val applicablePeople: String,
    @ColumnInfo(name = "medication_id") val medicationId: Int?,
    @ColumnInfo(name = "uuid") val uuid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "sync_status") val syncStatus: String = "created"
)