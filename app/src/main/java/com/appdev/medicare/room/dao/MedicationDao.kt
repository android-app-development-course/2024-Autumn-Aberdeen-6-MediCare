package com.appdev.medicare.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.appdev.medicare.room.entity.Medication

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medication")
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

    @Query("UPDATE medication SET sync_status = 'deleted' WHERE id = :id")
    fun softDeleteById(id: Int)
}