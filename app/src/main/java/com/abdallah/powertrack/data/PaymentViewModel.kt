package com.abdallah.powertrack.data

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PaymentViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    fun processTopUp(amount: Float, method: String, pinEntered: String, context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
        val savedPin = prefs.getString("transaction_pin", "1234")

        if (pinEntered != savedPin) {
            onError("Incorrect Transaction PIN")
            return
        }
        
        _isProcessing.value = true

        val unitsAdded = amount / 10.0f // 10 KES per unit
        
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        val date = sdf.format(Date())
        val historyEntry = "$date: Purchased $unitsAdded units via $method\n"

        val updateData = mapOf(
            "remainingUnits" to FieldValue.increment(unitsAdded.toDouble()),
            "lastTopUp" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(userId).update(updateData)
            .addOnSuccessListener {
                val current = prefs.getFloat("remaining_units", 0f)
                val oldHistory = prefs.getString("history", "") ?: ""
                
                prefs.edit {
                    putFloat("remaining_units", current + unitsAdded)
                    putString("history", historyEntry + oldHistory)
                }
                
                _isProcessing.value = false
                onSuccess()
            }
            .addOnFailureListener {
                _isProcessing.value = false
                onError("Payment failed: ${it.message}")
            }
    }
}
