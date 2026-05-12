package com.abdallah.powertrack.database.dao

import androidx.room.*
import com.abdallah.powertrack.database.entities.UserBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface UserBalanceDao {
    @Query("SELECT * FROM user_balance WHERE id = 0")
    fun getBalance(): Flow<UserBalance?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalance(balance: UserBalance)
}
