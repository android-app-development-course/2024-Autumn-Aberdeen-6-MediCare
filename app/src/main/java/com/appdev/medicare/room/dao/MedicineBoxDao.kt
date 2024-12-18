package com.appdev.medicare.room.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.appdev.medicare.room.entity.MedicineBox


@Dao
interface MedicineBoxDao {
    @Query("SELECT * FROM medicine_box WHERE sync_status != 'deleted'")
    fun getAll(): List<MedicineBox>

    @Query("SELECT * FROM medicine_box WHERE (id = :id AND sync_status != 'deleted')")
    fun findById(id: Int): MedicineBox

    @Query("SELECT id FROM medicine_box WHERE uuid = :uuid LIMIT 1")
    fun findIdByUuid(uuid: String): Int

    @Query("SELECT * FROM medicine_box WHERE +" +
            "(box_name = (:name) AND sync_status != 'deleted')")
    fun findByName(name: String): List<MedicineBox>

    @Query("SELECT medication_id FROM medicine_box WHERE +" +
            "(box_name = (:name) AND sync_status != 'deleted')")
    fun findMedicationListByName(name: String): List<Int>

    @Insert
    fun insertOne(medication: MedicineBox): Long

    @Query("UPDATE medicine_box SET sync_status = 'deleted' WHERE id = :id")
    fun softDeleteById(id: Int)

    @Query("DELETE FROM medicine_box WHERE sync_status = 'deleted'")
    fun deleteDeletedItems()

    @Query("UPDATE medicine_box SET sync_status = 'synced'")
    fun setAllSynced()

    @Transaction
    fun finishSync() {
        deleteDeletedItems()
        setAllSynced()
    }
}