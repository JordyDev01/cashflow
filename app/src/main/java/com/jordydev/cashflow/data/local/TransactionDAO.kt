package com.jordydev.cashflow.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jordydev.cashflow.util.Frequency
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query(
        """
    SELECT * FROM transactions 
    WHERE date = :date AND title = :title AND frequency = :frequency 
    AND isDeletedByUser = 0
    LIMIT 1
"""
    )
     fun findExactTransaction(
        date: String,
        title: String,
        frequency: Frequency
    ): Flow<List<TransactionEntity?>>

//    @Query("SELECT * FROM transactions ORDER BY date DESC")
//    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE title = :title AND frequency = :frequency AND isGenerated = 1 ORDER BY date DESC LIMIT 1")
    suspend fun getLatestGeneratedTransaction(title: String, frequency: Frequency): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE title = :title AND frequency = :frequency ORDER BY date")
    suspend fun getLatestByTitleAndFrequency(title: String, frequency: Frequency): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE date <= :today ORDER BY date DESC")
    fun getPastTransactions(today: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date > :today and isDeletedByUser = 0 ORDER BY date ASC")
    fun getFutureTransactions(today: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date = :date")
     fun getTransactionsForDay(date: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate ORDER BY date DESC")
    fun getTransactionsFrom(startDate: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate and isDeletedByUser = 0 ORDER BY date DESC")
     fun getTransactionsForRange(startDate: String, endDate: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE frequency != 'ONCE'")
     fun getRecurringTransactions(): Flow<List<TransactionEntity>>

    @Query("""
  SELECT * FROM transactions
  WHERE date = :date
    AND title = :title
    AND frequency = :frequency
  LIMIT 1
""")
    suspend fun findAnyExactTransaction(
        date: String,
        title: String,
        frequency: Frequency
    ): TransactionEntity?
}