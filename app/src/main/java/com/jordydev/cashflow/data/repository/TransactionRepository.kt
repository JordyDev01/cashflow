package com.jordydev.cashflow.data.repository


import android.os.Build
import androidx.annotation.RequiresApi

import com.jordydev.cashflow.data.local.TransactionDao
import com.jordydev.cashflow.data.local.TransactionEntity
import com.jordydev.cashflow.util.Frequency
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class TransactionRepository(
    private val dao: TransactionDao,
//    private val deletedInstanceDao: DeletedInstanceDao
) {
    fun getFutureTransactions(): Flow<List<TransactionEntity>> {
        val today = LocalDate.now().toString()
        return dao.getFutureTransactions(today)
    }

    suspend fun findAnyExactTransaction(
        date: String,
        title: String,
        frequency: Frequency
    ) = dao.findAnyExactTransaction(date, title, frequency)


     fun findExactTransaction(date: String, title: String, frequency: Frequency): Flow<List<TransactionEntity?>> {
        return dao.findExactTransaction(date, title, frequency)
    }

    // Filtered by date range
     fun getTransactionsForRange(startDate: String, endDate: String): Flow<List<TransactionEntity>> {
        return dao.getTransactionsForRange(startDate, endDate)
    }

    // Recurring transactions
     fun getRecurringTransactions(): Flow<List<TransactionEntity>> {
        return dao.getRecurringTransactions()
    }

    suspend fun getLatestGeneratedTransaction(title: String, frequency: Frequency): TransactionEntity? {
        return dao.getLatestGeneratedTransaction(title, frequency)
    }
//
//    suspend fun getLatestTransactionByTitleAndFrequency(title: String, frequency: Frequency): TransactionEntity? {
//        return dao.getLatestByTitleAndFrequency(title, frequency.name)
//    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
    }

    // Insert or Update (if using primary key id)
    suspend fun insertTransaction(transaction: TransactionEntity) {
        dao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        dao.updateTransaction(transaction)
    }

//    suspend fun isDeleted(title: String, date: String): Boolean {
//        return deletedInstanceDao.exists(title, date)
//    }
//
//    suspend fun markAsDeleted(title: String, date: String) {
//        deletedInstanceDao.insert(DeletedInstance(
//            title = title,
//            date = date
//        ))
//    }
}

