package com.example.fruitifyai.data


import kotlinx.coroutines.flow.Flow

// The repository needs a DAO instance injected in the constructor
class ScanResultRepository(private val scanResultDao: ScanResultDao) {

    val allScanResults: Flow<List<ScanResultEntity>> = scanResultDao.getAllScanResults()

    suspend fun insertScanResult(scanResult: ScanResultEntity) {
        scanResultDao.insertScanResult(scanResult)
    }

    suspend fun deleteAll() {
        scanResultDao.deleteAll()
    }

    // NEW: Delete individual scan result
    suspend fun deleteScanResultById(id: Int) {
        scanResultDao.deleteScanResultById(id)
    }

    // NEW: Pin/unpin a scan result
    suspend fun updatePinnedState(id: Int, pinned: Boolean) {
        scanResultDao.updatePinnedState(id, pinned)
    }
}