package com.appdev.medicare.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.appdev.medicare.room.dao.CalendarMedicationDao
import com.appdev.medicare.room.dao.MedicationDao
import com.appdev.medicare.room.dao.MedicationTimeDao
import com.appdev.medicare.room.dao.MedicineBoxDao
import com.appdev.medicare.room.entity.CalendarMedication
import com.appdev.medicare.room.entity.Medication
import com.appdev.medicare.room.entity.MedicationTime
import com.appdev.medicare.room.entity.MedicineBox

@Database(
    entities = [
        Medication::class,
        CalendarMedication::class,
        MedicationTime::class,
        MedicineBox::class,
    ],
    version = 5
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun calendarMedicationDao(): CalendarMedicationDao
    abstract fun medicationTimeDao(): MedicationTimeDao
    abstract fun medicineBoxDao(): MedicineBoxDao
}