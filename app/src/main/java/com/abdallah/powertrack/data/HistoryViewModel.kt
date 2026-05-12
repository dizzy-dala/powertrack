package com.abdallah.powertrack.data

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.powertrack.database.entities.TransactionEntity
import com.abdallah.powertrack.models.TransactionHistoryItem
import com.abdallah.powertrack.network.RetrofitClient
import com.abdallah.powertrack.repository.PowerRepository
import com.abdallah.powertrack.utils.NetworkMonitor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PowerRepository(application)
    private val networkMonitor = NetworkMonitor(application)
    private val _transactions = mutableStateOf<List<TransactionHistoryItem>>(emptyList())
    val transactions: State<List<TransactionHistoryItem>> = _transactions

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        viewModelScope.launch {
            repository.getTransactions().collectLatest { entities ->
                _transactions.value = entities.map { it.toModel() }
            }
        }

        // Auto-refresh when internet returns
        viewModelScope.launch {
            networkMonitor.isConnected.collectLatest { connected ->
                if (connected) {
                    val prefs = application.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
                    val meterNumber = prefs.getString("meter_number", "") ?: ""
                    if (meterNumber.isNotBlank()) {
                        repository.refreshHistory(meterNumber)
                    }
                }
            }
        }
    }

    private fun TransactionEntity.toModel() = TransactionHistoryItem(
        id = id,
        amount = amount,
        units = units,
        token = token,
        status = status,
        date = date
    )

    fun loadFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
        val json = prefs.getString("transaction_history", null)
        if (json != null) {
            val type = object : TypeToken<List<TransactionHistoryItem>>() {}.type
            _transactions.value = Gson().fromJson(json, type)
        }
    }

    fun fetchHistory(meterNumber: String, context: Context? = null) {
        if (meterNumber.isBlank()) return
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                repository.refreshHistory(meterNumber)
            } catch (e: Exception) {
                _error.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
