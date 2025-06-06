package com.jordydev.cashflow.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("""
    SELECT * FROM transactions 
    WHERE date = :date AND title = :title AND frequency = :frequency 
    LIMIT 1
""")
    suspend fun findExactTransaction(
        date: String,
        title: String,
        frequency: String
    ): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE title = :title AND frequency = :frequency AND isGenerated = 1 ORDER BY date DESC LIMIT 1")
    suspend fun getLatestGeneratedTransaction(title: String, frequency: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE title = :title AND frequency = :frequency ORDER BY date")
    suspend fun getLatestByTitleAndFrequency(title: String, frequency: String): TransactionEntity?


    @Query("SELECT * FROM transactions WHERE date <= :today ORDER BY date DESC")
    fun getPastTransactions(today: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date > :today ORDER BY date ASC")
    fun getFutureTransactions(today: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date = :date")
     fun getTransactionsForDay(date: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate ORDER BY date DESC")
    fun getTransactionsFrom(startDate: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate")
     fun getTransactionsForRange(startDate: String, endDate: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE frequency != 'ONCE'")
     fun getRecurringTransactions(): Flow<List<TransactionEntity>>
}