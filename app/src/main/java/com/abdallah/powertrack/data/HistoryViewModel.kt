package com.abdallah.powertrack.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.powertrack.models.TransactionHistoryItem
import com.abdallah.powertrack.network.RetrofitClient
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val _transactions = mutableStateOf<List<TransactionHistoryItem>>(emptyList())
    val transactions: State<List<TransactionHistoryItem>> = _transactions

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchHistory(meterNumber: String) {
        if (meterNumber.isBlank()) return
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getHistory(meterNumber)
                if (response.isSuccessful) {
                    _transactions.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to fetch history: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
