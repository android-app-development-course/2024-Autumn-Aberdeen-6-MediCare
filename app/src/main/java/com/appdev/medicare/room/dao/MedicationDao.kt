package com.appdev.medicare.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.appdev.medicare.room.entity.Medication

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medication WHERE sync_status != 'deleted'")
    fun getAll(): List<Medication>

    @Query("SELECT * FROM medication WHERE (id = :id AND sync_status != 'deleted')")
    fun findById(id: Int): Medication

    @Query("SELECT id FROM medication WHERE uuid = :uuid LIMIT 1")
    fun findIdByUuid(uuid: String): Int

    @Query("SELECT uuid FROM medication WHERE id = :id LIMIT 1")
    fun findUuidById(id: Int): String

    @Insert
//    fun insertOne(vararg medications: Medication): Long
    fun insertOne(medication: Medication): Long

    @Query("UPDATE medication SET patient_name = :patientName, " +
            "dosage = :dosage, remaining_amount = :remainingAmount, " +
            "frequency = :frequency, expiration_date = :expiryDate " +
            "WHERE id = :id")
    fun updateMedicationRecord(id: Int, patientName: String, dosage: String, remainingAmount: String,
                               frequency: String, expiryDate: String): Int

    @Query("UPDATE medication SET sync_status = 'deleted' WHERE id = :id")
    fun softDeleteById(id: Int)

    @Query("DELETE FROM medication WHERE sync_status = 'deleted'")
    fun deleteDeletedItems()

    @Query("UPDATE medication SET sync_status = 'synced'")
    fun setAllSynced()

    @Transaction
    fun finishSync() {
        deleteDeletedItems()
        setAllSynced()
    }
}