package com.abdallah.powertrack.model

data class User(
    val name: String = "",
    val email: String = "",
    val uid: String = "",
    val remainingUnits: Float = 0f,
    val dailyUsage: Float = 1.2f,
    val isSetupComplete: Boolean = false,
    val transactionPin: String = "1234" // Default for new users
)
