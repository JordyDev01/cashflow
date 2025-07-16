package com.jordydev.cashflow.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jordydev.cashflow.util.Frequency
import com.jordydev.cashflow.util.TransactionType


@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val frequency: Frequency,
    val date: String,
    val isGenerated: Boolean = false,
    val nextDueDate: String? = null,
    @ColumnInfo(defaultValue = "0")
    val isDeletedByUser: Boolean = false
)