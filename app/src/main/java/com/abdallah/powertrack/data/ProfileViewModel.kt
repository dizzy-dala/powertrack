package com.abdallah.powertrack.data

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.powertrack.models.UserProfile
import com.abdallah.powertrack.network.RetrofitClient
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _userProfile = mutableStateOf<UserProfile?>(null)
    val userProfile: State<UserProfile?> = _userProfile

    private val _isUpdating = mutableStateOf(false)
    val isUpdating: State<Boolean> = _isUpdating

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    fun fetchProfile(context: Context) {
        val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
        val name = prefs.getString("user_name", "User") ?: "User"
        val phone = prefs.getString("phone_number", "") ?: ""
        val meter = prefs.getString("meter_number", "") ?: ""
        
        // Initial state from local prefs
        _userProfile.value = UserProfile(name = name, phoneNumber = phone, meterNumber = meter)

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getProfile()
                if (response.isSuccessful && response.body() != null) {
                    _userProfile.value = response.body()
                    saveToPrefs(context, response.body()!!)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch profile: ${e.localizedMessage}"
            }
        }
    }

    fun updateProfile(context: Context, updatedProfile: UserProfile, onSuccess: () -> Unit) {
        _isUpdating.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updateProfile(updatedProfile)
                if (response.isSuccessful) {
                    _userProfile.value = updatedProfile
                    saveToPrefs(context, updatedProfile)
                    onSuccess()
                } else {
                    _errorMessage.value = "Update failed: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Update error: ${e.localizedMessage}"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    private fun saveToPrefs(context: Context, profile: UserProfile) {
        val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("user_name", profile.name)
            putString("phone_number", profile.phoneNumber)
            putString("meter_number", profile.meterNumber)
            apply()
        }
    }
}
