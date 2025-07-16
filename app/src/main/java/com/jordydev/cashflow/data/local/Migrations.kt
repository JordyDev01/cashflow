package com.jordydev.cashflow.data.local
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppMigrations {
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE transactions ADD COLUMN isDeletedByUser INTEGER NOT NULL")
        }
    }


    val all = arrayOf(MIGRATION_4_5)
}
