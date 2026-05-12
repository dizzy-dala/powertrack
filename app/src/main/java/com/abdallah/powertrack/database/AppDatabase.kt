package com.abdallah.powertrack.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abdallah.powertrack.database.dao.TransactionDao
import com.abdallah.powertrack.database.dao.UserBalanceDao
import com.abdallah.powertrack.database.entities.TransactionEntity
import com.abdallah.powertrack.database.entities.UserBalance

@Database(entities = [UserBalance::class, TransactionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userBalanceDao(): UserBalanceDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "powertrack_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
