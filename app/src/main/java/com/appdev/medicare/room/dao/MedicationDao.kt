package com.appdev.medicare.room.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.appdev.medicare.room.entity.Medication

interface MedicationDao {
    @Query("SELECT * FROM medication")
    fun getAll(): List<Medication>

    @Query("SELECT * FROM medication WHERE id = (:id)")
    fun findById(id: Int): List<Medication>

    @Insert
    fun insertOne(vararg medications: Medication)

    @Delete
    fun delete(medication: Medication)
}