package com.example.fruitifyai.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fruitifyai.data.ScanResultEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {

    @Query("SELECT * FROM scan_results ORDER BY pinned DESC, timestamp DESC")
    fun getAllScanResults(): Flow<List<ScanResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanResult(scanResult: ScanResultEntity)

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun deleteScanResultById(id: Int)

    @Query("UPDATE scan_results SET pinned = :pinned WHERE id = :id")
    suspend fun updatePinnedState(id: Int, pinned: Boolean)

    @Query("DELETE FROM scan_results")
    suspend fun deleteAll()
}