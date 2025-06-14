package com.jordydev.cashflow.presentation.homeScreen


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jordydev.cashflow.data.local.TransactionEntity

import com.jordydev.cashflow.data.repository.TransactionRepository
import com.jordydev.cashflow.util.Frequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate



@RequiresApi(Build.VERSION_CODES.O)

class TransactionViewModel (
    private val repository: TransactionRepository
) : ViewModel() {


    private val _transactionState = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactionState: StateFlow<List<TransactionEntity>> = _transactionState

    private val _showFutureExpenses = MutableStateFlow(false)

    private var lastRangeLabel: String? = null
    private var lastFrequency: Frequency? = null


    fun toggleShowFutureExpenses(show: Boolean) {
        _showFutureExpenses.value = show

        if (show) {
            generateFutureRecurringTransactions()
        }

        loadRelevantTransactions()
    }

    val totalIncome: StateFlow<Double> = transactionState.map { txns ->
        txns.filter { it.type.name == "INCOME" }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalExpenses: StateFlow<Double> = transactionState.map { txns ->
        txns.filter { it.type.name == "EXPENSE" }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val balance: StateFlow<Double> = transactionState.map { txns ->
        txns
            .filter { LocalDate.parse(it.date) <= LocalDate.now() }
            .sumOf { if (it.type.name == "INCOME") it.amount else -it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)


    @RequiresApi(Build.VERSION_CODES.O)
    fun generateFutureRecurringTransactions(daysAhead: Long = 30) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val futureLimit = today.plusDays(daysAhead)

            repository.getRecurringTransactions().collectLatest { recurringTransactions ->
                recurringTransactions.forEach { txn ->
                    val baseDate = repository.getLatestGeneratedTransaction(txn.title, txn.frequency)
                        ?.let { LocalDate.parse(it.date) }
                        ?: LocalDate.parse(txn.date)

                    val increment: (LocalDate) -> LocalDate = when (txn.frequency) {
                        Frequency.DAILY -> { d -> d.plusDays(1) }
                        Frequency.WEEKLY -> { d -> d.plusWeeks(1) }
                        Frequency.BIWEEKLY -> { d -> d.plusWeeks(2) }
                        Frequency.MONTHLY -> { d -> d.plusMonths(1) }
                        else -> return@forEach
                    }

                    var current = increment(baseDate)
                    var count = 0
                    val maxIterations = 1000

                    while (current <= futureLimit && count < maxIterations) {
                        val exists = repository.findExactTransaction(
                            date = current.toString(),
                            title = txn.title,
                            frequency = txn.frequency
                        )
                        if (exists == null) {
                            val newTxn = txn.copy(
                                id = 0,
                                date = current.toString(),
                                isGenerated = true
                            )
                            repository.insertTransaction(newTxn)
                            Log.d("FutureTxn", "Pre-generated ${newTxn.title} for ${newTxn.date}")
                        }

                        current = increment(current)
                        count++
                    }
                }
            }
        }
    }



    fun loadRelevantTransactions(rangeLabel: String? = null, frequency: Frequency? = null) {
        lastRangeLabel = rangeLabel ?: lastRangeLabel
        lastFrequency = frequency // ✅ update this even if it's null

        viewModelScope.launch {
            val flow = if (_showFutureExpenses.value) {
                repository.getFutureTransactions()
            } else {
                val today = LocalDate.now()
                val startDate = when (lastRangeLabel) {
                    "Last 3 Months" -> today.minusMonths(3)
                    "Last 6 Months" -> today.minusMonths(6)
                    "Last 12 Months" -> today.minusMonths(12)
                    else -> today.minusDays(14)
                }.toString()
                val endDate = today.toString()
                repository.getTransactionsForRange(startDate, endDate)
            }

            flow.collect { list ->
                Log.d("DEBUG", "Got ${list.size} transactions: ${list.map { it.date }}")
                _transactionState.value = filterByFrequency(list)
                    .sortedByDescending { LocalDate.parse(it.date) }
            }
        }
    }



    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            loadRelevantTransactions()
        }
    }


    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

//    fun getTransactionsForRange(startDate: String, endDate: String): Flow<List<TransactionEntity>> {
//        return repository.getTransactionsForRange(startDate, endDate)
//    }
//
//    fun getRecurringTransactions(): Flow<List<TransactionEntity>> {
//        return repository.getRecurringTransactions()
//    }

    private fun filterByFrequency(transactions: List<TransactionEntity>): List<TransactionEntity> {
        return lastFrequency?.let { f ->
            transactions.filter { it.frequency == f }
        } ?: transactions
    }

    fun setSelectedDateRange(label: String) {
        lastRangeLabel = label
        loadRelevantTransactions()
    }

    fun setSelectedFrequency(freq: Frequency?) {
        lastFrequency = freq
        loadRelevantTransactions()
    }
}
