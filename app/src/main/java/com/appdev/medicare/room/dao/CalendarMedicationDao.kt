package com.appdev.medicare.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.appdev.medicare.room.entity.CalendarMedication

@Dao
interface CalendarMedicationDao {
    @Query("SELECT * FROM calendar_medication")
    fun getAll(): List<CalendarMedication>

    @Query("SELECT * FROM calendar_medication WHERE (medication_id = :medicationId AND sync_status != 'deleted')")
    fun findAllByMedicationId(medicationId: Int): List<CalendarMedication>

    @Query("SELECT medication_id FROM calendar_medication WHERE date = :date AND sync_status != 'deleted'")
    fun findMedicationIdByDate(date: String): List<Int>

    @Query("SELECT id FROM calendar_medication WHERE medication_id = (:medicationId) AND date = (:date) AND sync_status != 'deleted'")
    fun findId(medicationId: Int, date:String): Int

    @Insert
//    fun insertOne(vararg calendarMedications: CalendarMedication): Long
    fun insertOne(calendarMedication: CalendarMedication): Long

    @Query("UPDATE calendar_medication SET sync_status = 'deleted' WHERE id = :id")
    fun softDeleteById(id: Int)

    @Query("UPDATE calendar_medication SET sync_status = 'deleted' WHERE medication_id = (:medicationId) AND date = (:date)")
    fun softDeleteByMedicationIdAndDate(medicationId: Int, date: String)
}