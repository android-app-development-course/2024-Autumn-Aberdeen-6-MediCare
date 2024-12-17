package com.appdev.medicare.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.appdev.medicare.room.entity.Medication

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medication")
    fun getAll(): List<Medication>

    @Query("SELECT * FROM medication WHERE id = (:id)")
    fun findById(id: Int): Medication

    @Insert
//    fun insertOne(vararg medications: Medication): Long
    fun insertOne(medication: Medication): Long

    @Delete
    fun delete(medication: Medication)
}