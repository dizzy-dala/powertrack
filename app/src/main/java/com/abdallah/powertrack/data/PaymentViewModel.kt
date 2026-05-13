package com.abdallah.powertrack.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.powertrack.models.BuyTokenRequest
import com.abdallah.powertrack.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PaymentViewModel : ViewModel() {
    private val TAG = "PaymentViewModel"
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    fun processTopUp(
        amount: Float,
        method: String,
        phoneNumber: String,
        meterNumber: String,
        context: Context,
        onSuccess: (token: String, units: Float) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        _isProcessing.value = true

        viewModelScope.launch {
            try {
                // Formatting amount as Int as requested by the user
                val request = BuyTokenRequest(
                    amount = amount.toInt(),
                    phoneNumber = phoneNumber,
                    meterNumber = meterNumber
                )
                val response = RetrofitClient.instance.buyToken(request)

                Log.d(TAG, "Response Code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Log.d(TAG, "Response Body: $body")
                    
                    if (body.status == "success") {
                        val checkoutId = body.checkoutRequestId
                        if (checkoutId != null && method == "M-Pesa") {
                            // Start polling for the actual result
                            pollTransactionStatus(checkoutId, meterNumber, onSuccess, onError)
                        } else {
                            // Non-Mpesa or instant success (simulated)
                            val token = body.token ?: generateFakeToken()
                            val units = body.units ?: (amount / 25.0f)
                            saveTokenToFirestore(userId, meterNumber, amount, token, units)
                            _isProcessing.value = false
                            onSuccess(token, units)
                        }
                    } else {
                        _isProcessing.value = false
                        onError(body.message)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error Body: $errorBody")
                    _isProcessing.value = false
                    onError("Server Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network Error", e)
                _isProcessing.value = false
                onError("Network Error: ${e.localizedMessage}")
            }
        }
    }

    private fun pollTransactionStatus(
        checkoutRequestId: String,
        meterNumber: String,
        onSuccess: (String, Float) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 20 // Poll for about 60 seconds (20 * 3s)
            
            while (attempts < maxAttempts) {
                delay(3000) // Wait 3 seconds between polls
                attempts++
                
                try {
                    val response = RetrofitClient.instance.checkStatus(checkoutRequestId)
                    Log.d(TAG, "Poll Status Response: ${response.code()}")
                    
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        Log.d(TAG, "Poll Status Body: $body")
                        when (body.status.lowercase()) {
                            "success" -> {
                                val token = body.token ?: generateFakeToken()
                                val units = body.units ?: 0f
                                val amount = body.amount ?: 0f
                                
                                saveTokenToFirestore(userId, meterNumber, amount, token, units)
                                
                                _isProcessing.value = false
                                onSuccess(token, units)
                                return@launch
                            }
                            "failed" -> {
                                _isProcessing.value = false
                                onError(body.message)
                                return@launch
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Log error and continue polling
                }
            }
            
            _isProcessing.value = false
            onError("Payment confirmation timed out. Check your M-Pesa for a confirmation SMS.")
        }
    }

    private fun saveTokenToFirestore(
        userId: String,
        meterNumber: String,
        amount: Float,
        token: String,
        units: Float
    ) {
        val transaction = hashMapOf(
            "userId" to userId,
            "meterNumber" to meterNumber,
            "amount" to amount,
            "token" to token,
            "units" to units,
            "timestamp" to System.currentTimeMillis(),
            "date" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        db.collection("tokens")
            .add(transaction)
            .addOnSuccessListener {
                // Also update the user's remaining units in their profile
                db.collection("users").document(userId).get().addOnSuccessListener { snapshot ->
                    val currentUnits = snapshot.getDouble("remainingUnits") ?: 0.0
                    db.collection("users").document(userId)
                        .update("remainingUnits", currentUnits + units)
                }
            }
    }

    private fun generateFakeToken(): String {
        return (1..5).joinToString("-") { 
            (1000..9999).random().toString()
        }
    }
}
