package com.example.fruitifyai.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ScanResultEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanResultDao(): ScanResultDao
}