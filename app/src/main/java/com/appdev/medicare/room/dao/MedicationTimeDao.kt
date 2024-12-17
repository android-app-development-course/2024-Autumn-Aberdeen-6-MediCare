package com.appdev.medicare.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.appdev.medicare.room.entity.MedicationTime

@Dao
interface MedicationTimeDao {
    @Query("SELECT * FROM medication_time")
    fun getAll(): List<MedicationTime>

    @Query("SELECT * FROM medication_time WHERE (id = :id AND sync_status != 'deleted')")
    fun findById(id: Int): List<MedicationTime>

    @Query("SELECT * FROM medication_time WHERE " +
            "medication_id = :medicationId AND date_id = :dateId")
    fun findByMedicationAndDateId(medicationId: Int, dateId: Int): List<MedicationTime>

    @Insert
//    fun insertOne(vararg medications: MedicationTime): Long
    fun insertOne(medication: MedicationTime): Long

    @Query("UPDATE medication_time SET sync_status = 'deleted' WHERE id = :id")
    fun softDeleteById(id: Int)
}