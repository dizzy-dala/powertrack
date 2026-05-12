package com.abdallah.powertrack.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_balance")
data class UserBalance(
    @PrimaryKey val id: Int = 0, // We only store one balance record for the user
    val units: Float,
    val dailyUsage: Float,
    val userName: String
)
