package com.abdallah.powertrack.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: Int,
    val amount: Float,
    val units: Float,
    val token: String,
    val status: String,
    val date: String
)
