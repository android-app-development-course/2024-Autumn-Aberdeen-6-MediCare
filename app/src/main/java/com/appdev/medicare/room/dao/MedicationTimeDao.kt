package com.appdev.medicare.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.appdev.medicare.room.entity.MedicationTime

@Dao
interface MedicationTimeDao {
    @Query("SELECT * FROM medication_time WHERE sync_status != 'deleted'")
    fun getAll(): List<MedicationTime>

    @Query("SELECT * FROM medication_time WHERE (id = :id AND sync_status != 'deleted')")
    fun findById(id: Int): List<MedicationTime>

    @Query("SELECT * FROM medication_time WHERE " +
            "medication_id = :medicationId AND date_id = :dateId AND sync_status != 'deleted'")
    fun findByMedicationAndDateId(medicationId: Int, dateId: Int): List<MedicationTime>

    @Query("SELECT id FROM medication_time WHERE uuid = :uuid LIMIT 1")
    fun fundIdByUuid(uuid: String): Int

    @Query("SELECT uuid FROM medication_time WHERE id = :id LIMIT 1")
    fun findUuidById(id: Int): String

    @Insert
//    fun insertOne(vararg medications: MedicationTime): Long
    fun insertOne(medication: MedicationTime): Long

    @Query("UPDATE medication_time SET sync_status = 'deleted' WHERE id = :id")
    fun softDeleteById(id: Int)

    @Query("DELETE FROM medication_time WHERE sync_status = 'deleted'")
    fun deleteDeletedItems()

    @Query("UPDATE medication_time SET sync_status = 'synced'")
    fun setAllSynced()

    @Transaction
    fun finishSync() {
        deleteDeletedItems()
        setAllSynced()
    }
}