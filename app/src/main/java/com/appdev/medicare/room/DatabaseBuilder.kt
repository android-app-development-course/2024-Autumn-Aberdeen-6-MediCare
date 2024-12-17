package com.appdev.medicare.room

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context? = null): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context!!.applicationContext,
                AppDatabase::class.java,
                "medicare_database"
            )
                .fallbackToDestructiveMigration()       // 数据库版本更新时重建数据库
                .build()
            INSTANCE = instance
            instance
        }
    }
}