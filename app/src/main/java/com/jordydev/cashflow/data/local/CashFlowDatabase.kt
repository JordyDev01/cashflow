package com.jordydev.cashflow.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TransactionEntity::class, DeletedInstance::class], version = 4, exportSchema = false )
abstract class CashFlowDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun deletedInstanceDao(): DeletedInstanceDao

    companion object {
        @Volatile private var INSTANCE: CashFlowDatabase? = null

        fun getDatabase(context: Context): CashFlowDatabase {
            try {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CashFlowDatabase::class.java,
                    "cashflow_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            } catch (e: Exception) {
                Log.e("DB_INIT", "❌ Room failed: ${e.message}", e)
                throw e // or handle gracefully
            }
            return INSTANCE!!
        }
    }
}