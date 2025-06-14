package com.jordydev.cashflow.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TransactionEntity::class, DeletedInstance::class], version = 5, exportSchema = false )
@TypeConverters(Converters ::class)
abstract class CashFlowDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao


    companion object {
        @Volatile private var INSTANCE: CashFlowDatabase? = null

        fun getDatabase(context: Context): CashFlowDatabase {
            try {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CashFlowDatabase::class.java,
                    "cashflow_db"
                )
                    .addMigrations(AppMigrations.MIGRATION_4_5)
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