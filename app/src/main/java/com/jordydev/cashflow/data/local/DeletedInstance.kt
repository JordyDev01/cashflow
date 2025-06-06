package com.jordydev.cashflow.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_instance")
data class DeletedInstance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String
)