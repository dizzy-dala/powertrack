package com.abdallah.powertrack.data

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.viewModelScope
import androidx.core.content.edit
import com.abdallah.powertrack.model.User
import com.abdallah.powertrack.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    private val _units = mutableFloatStateOf(0f)
    val units: State<Float> = _units

    private val _dailyUsage = mutableFloatStateOf(1.2f)
    val dailyUsage: State<Float> = _dailyUsage

    private val _userName = mutableStateOf("User")
    val userName: State<String> = _userName

    fun loadFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
        _units.floatValue = prefs.getFloat("remaining_units", 0f)
        _dailyUsage.floatValue = prefs.getFloat("daily_usage", 1.2f)
        _userName.value = prefs.getString("user_name", "User") ?: "User"
    }

    fun refreshFromBackend(meterNumber: String, context: Context? = null) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getBalance(meterNumber)
                if (response.isSuccessful && response.body() != null) {
                    val newBalance = response.body()!!.balance
                    _units.floatValue = newBalance
                    
                    // Persist to SharedPreferences if context is provided
                    context?.getSharedPreferences("powertrack", Context.MODE_PRIVATE)?.edit {
                        putFloat("remaining_units", newBalance)
                    }
                }
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }

    fun startListening(context: Context) {
        val userId = auth.currentUser?.uid ?: return
        
        listener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    if (user != null) {
                        _units.floatValue = user.remainingUnits
                        _dailyUsage.floatValue = user.dailyUsage
                        _userName.value = user.name
                        
                        // Sync to local prefs for offline access
                        val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
                        prefs.edit {
                            putFloat("remaining_units", user.remainingUnits)
                            putFloat("daily_usage", user.dailyUsage)
                            putString("user_name", user.name)
                            putString("transaction_pin", user.transactionPin)
                        }
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
