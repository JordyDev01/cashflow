package com.jordydev.cashflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeletedInstanceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(deleted: DeletedInstance)

    @Query("SELECT EXISTS(SELECT 1 FROM deleted_instance WHERE title = :title AND date = :date LIMIT 1)")
    suspend fun exists(title: String, date: String): Boolean
}