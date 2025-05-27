package com.example.fruitifyai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fruitifyai.data.ScanResultEntity
import com.example.fruitifyai.data.ScanResultRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScanResultViewModel(
    private val repository: ScanResultRepository
) : ViewModel() {

    val scanResults = repository.allScanResults

    fun insert(scanResult: ScanResultEntity) = viewModelScope.launch {
        repository.insertScanResult(scanResult)
    }

    fun deleteById(id: Int) = viewModelScope.launch {
        repository.deleteScanResultById(id)
    }

    fun updatePinned(id: Int, pinned: Boolean) = viewModelScope.launch {
        repository.updatePinnedState(id, pinned)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}

// Factory class to create ScanResultViewModel with repository param
class ScanResultViewModelFactory(
    private val repository: ScanResultRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScanResultViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}