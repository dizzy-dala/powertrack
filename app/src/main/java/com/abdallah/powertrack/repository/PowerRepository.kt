package com.abdallah.powertrack.repository

import android.content.Context
import com.abdallah.powertrack.database.AppDatabase
import com.abdallah.powertrack.database.entities.TransactionEntity
import com.abdallah.powertrack.database.entities.UserBalance
import com.abdallah.powertrack.models.TransactionHistoryItem
import com.abdallah.powertrack.network.RetrofitClient
import com.abdallah.powertrack.utils.NetworkUtils
import kotlinx.coroutines.flow.Flow

class PowerRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val userBalanceDao = database.userBalanceDao()
    private val transactionDao = database.transactionDao()
    private val apiService = RetrofitClient.instance

    // Balance
    fun getUserBalance(): Flow<UserBalance?> = userBalanceDao.getBalance()

    suspend fun insertLocalBalance(units: Float, dailyUsage: Float, userName: String) {
        userBalanceDao.insertBalance(
            UserBalance(
                units = units,
                dailyUsage = dailyUsage,
                userName = userName
            )
        )
    }

    suspend fun refreshBalance(meterNumber: String) {
        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                val response = apiService.getBalance(meterNumber)
                if (response.isSuccessful && response.body() != null) {
                    val balance = response.body()!!
                    // For now we use some default values for dailyUsage and userName if not provided by this API
                    // Or we could get them from SharedPreferences/Firestore as before
                    val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
                    val userName = prefs.getString("user_name", "User") ?: "User"
                    val dailyUsage = prefs.getFloat("daily_usage", 1.2f)
                    
                    userBalanceDao.insertBalance(
                        UserBalance(
                            units = balance.balance,
                            dailyUsage = dailyUsage,
                            userName = userName
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Transactions
    fun getTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun refreshHistory(meterNumber: String) {
        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                val response = apiService.getHistory(meterNumber)
                if (response.isSuccessful && response.body() != null) {
                    val history = response.body()!!
                    val entities = history.map { it.toEntity() }
                    transactionDao.clearAll()
                    transactionDao.insertTransactions(entities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun TransactionHistoryItem.toEntity() = TransactionEntity(
        id = id,
        amount = amount,
        units = units,
        token = token,
        status = status,
        date = date
    )
}
