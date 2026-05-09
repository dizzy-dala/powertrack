package com.abdallah.powertrack.data

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.core.content.edit
import com.abdallah.powertrack.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    fun signup(name: String, email: String, pass: String, context: Context, onSignupSuccess: () -> Unit) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val userData = User(name, email, userId)
                    
                    // Save to local preferences immediately for speed
                    val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
                    prefs.edit {
                        putString("user_name", name)
                        putString("user_email", email)
                        putBoolean("is_registered", true)
                    }

                    // Fire and forget Firestore update - don't wait for network
                    db.collection("users").document(userId).set(userData)
                    
                    _isLoading.value = false
                    onSignupSuccess()
                } else {
                    _isLoading.value = false
                    _errorMessage.value = task.exception?.message ?: "Signup failed"
                }
            }
    }

    fun login(email: String, pass: String, context: Context, onLoginSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            val user = document.toObject(User::class.java)
                            if (user != null) {
                                val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
                                prefs.edit {
                                    putString("user_name", user.name)
                                    putString("user_email", user.email)
                                    putFloat("remaining_units", user.remainingUnits)
                                    putFloat("daily_usage", user.dailyUsage)
                                    putString("transaction_pin", user.transactionPin)
                                    putBoolean("is_setup_complete", user.isSetupComplete)
                                    putBoolean("is_registered", true)
                                }
                            }
                            _isLoading.value = false
                            onLoginSuccess()
                        }
                        .addOnFailureListener {
                            _isLoading.value = false
                            onLoginSuccess()
                        }
                } else {
                    _isLoading.value = false
                    _errorMessage.value = task.exception?.message ?: "Login failed"
                }
            }
    }
}
