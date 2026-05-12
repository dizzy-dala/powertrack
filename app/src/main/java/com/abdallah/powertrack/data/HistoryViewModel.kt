package com.abdallah.powertrack.data

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.powertrack.models.TransactionHistoryItem
import com.abdallah.powertrack.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val _transactions = mutableStateOf<List<TransactionHistoryItem>>(emptyList())
    val transactions: State<List<TransactionHistoryItem>> = _transactions

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

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
                val response = RetrofitClient.instance.getHistory(meterNumber)
                if (response.isSuccessful) {
                    val history = response.body() ?: emptyList()
                    _transactions.value = history
                    
                    // Persist to SharedPreferences
                    context?.getSharedPreferences("powertrack", Context.MODE_PRIVATE)?.edit {
                        putString("transaction_history", Gson().toJson(history))
                    }
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
