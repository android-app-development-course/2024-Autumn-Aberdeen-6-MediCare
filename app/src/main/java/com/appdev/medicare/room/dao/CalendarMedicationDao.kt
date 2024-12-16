package com.appdev.medicare.room.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.appdev.medicare.room.entity.CalendarMedication

interface CalendarMedicationDao {
    @Query("SELECT * FROM calendar_medication")
    fun getAll(): List<CalendarMedication>

    @Query("SELECT * FROM calendar_medication WHERE medication_id = (:medicationId)")
    fun findAllByMedicationId(medicationId: Int): List<CalendarMedication>

    @Insert
    fun insertOne(vararg calendarMedications: CalendarMedication)

    @Delete
    fun delete(calendarMedication: CalendarMedication)
}