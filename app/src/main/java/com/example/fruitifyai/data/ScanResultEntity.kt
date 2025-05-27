package com.example.fruitifyai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fruitName: String,
    val freshness: String?,
    val confidence: Float,
    val timestamp: Long,
    val pinned: Boolean = false
)