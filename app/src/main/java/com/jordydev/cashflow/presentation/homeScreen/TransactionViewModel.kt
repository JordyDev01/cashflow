package com.jordydev.cashflow.presentation.homeScreen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jordydev.cashflow.data.local.TransactionEntity
import com.jordydev.cashflow.data.repository.TransactionRepository
import com.jordydev.cashflow.util.Frequency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactionState = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactionState: StateFlow<List<TransactionEntity>> = _transactionState

    private val _showFutureExpenses = MutableStateFlow(false)
    val showFutureExpenses: StateFlow<Boolean> = _showFutureExpenses

    private var lastRangeLabel: String? = null
    private var lastFrequency: Frequency? = null

    /**
     * Toggle between past and future transactions, resetting frequency filter.
     */
    fun toggleShowFutureExpenses(show: Boolean) {
        _showFutureExpenses.value = show
        // Clear any frequency filter to show all frequencies
        lastFrequency = null
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
        txns.filter { LocalDate.parse(it.date) <= LocalDate.now() }
            .sumOf { if (it.type.name == "INCOME") it.amount else -it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    /**
     * Pre-generate future recurring transactions up to [daysAhead], skipping any existing or soft-deleted ones.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateFutureRecurringTransactions(daysAhead: Long = 30) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val futureLimit = today.plusDays(daysAhead)

            repository.getRecurringTransactions().collectLatest { recurringList ->
                recurringList.forEach { template ->
                    val baseDate = repository.getLatestGeneratedTransaction(
                        template.title, template.frequency
                    )?.let { LocalDate.parse(it.date) }
                        ?: LocalDate.parse(template.date)

                    // Increment function based on frequency
                    val increment: (LocalDate) -> LocalDate = when (template.frequency) {
                        Frequency.DAILY    -> { d -> d.plusDays(1) }
                        Frequency.WEEKLY   -> { d -> d.plusWeeks(1) }
                        Frequency.BIWEEKLY -> { d -> d.plusWeeks(2) }
                        Frequency.MONTHLY  -> { d -> d.plusMonths(1) }
                        else               -> return@forEach
                    }

                    var current = increment(baseDate)
                    var count = 0
                    val maxIterations = 1000

                    while (current <= futureLimit && count < maxIterations) {
                        // Check any existing (including soft-deleted)
                        val existing = repository.findAnyExactTransaction(
                            date      = current.toString(),
                            title     = template.title,
                            frequency = template.frequency
                        )
                        if (existing == null) {
                            repository.insertTransaction(
                                template.copy(
                                    id = 0,
                                    date = current.toString(),
                                    isGenerated = true
                                )
                            )
                            Log.d("FutureTxn", "Generated ${template.title} for ${current}")
                        }
                        current = increment(current)
                        count++
                    }
                }
            }
        }
    }

    /**
     * Load either past or future transactions with optional range and frequency filtering.
     */
    fun loadRelevantTransactions(rangeLabel: String? = null, frequency: Frequency? = null) {
        lastRangeLabel = rangeLabel ?: lastRangeLabel
        lastFrequency = frequency // update, even if null to clear filter

        viewModelScope.launch {
            val flow = if (_showFutureExpenses.value) {
                repository.getFutureTransactions()
            } else {
                val today = LocalDate.now()
                val startDate = when (lastRangeLabel) {
                    "Last 3 Months" -> today.minusMonths(3)
                    "Last 6 Months" -> today.minusMonths(6)
                    "Last 12 Months"-> today.minusMonths(12)
                    else             -> today.minusDays(14)
                }.toString()
                repository.getTransactionsForRange(startDate, today.toString())
            }

            flow.collect { list ->
                Log.d("DEBUG", "Loaded ${list.size} txns: ${list.map { it.date }}")
                _transactionState.value = list
                    .let { filterByFrequency(it) }
                    .sortedByDescending { LocalDate.parse(it.date) }
            }
        }
    }

    fun addTransaction(txn: TransactionEntity) = viewModelScope.launch {
        repository.insertTransaction(txn)
    }

    fun deleteTransaction(txn: TransactionEntity) = viewModelScope.launch {
        if (txn.isGenerated) {
            repository.updateTransaction(txn.copy(isDeletedByUser = true))
        } else {
            repository.deleteTransaction(txn)
        }
        loadRelevantTransactions()
    }

    fun updateTransaction(txn: TransactionEntity) = viewModelScope.launch {
        repository.updateTransaction(txn)
    }

    private fun filterByFrequency(list: List<TransactionEntity>): List<TransactionEntity> =
        lastFrequency?.let { freq -> list.filter { it.frequency == freq } } ?: list

    fun setSelectedDateRange(label: String) {
        lastRangeLabel = label
        loadRelevantTransactions()
    }

    fun setSelectedFrequency(freq: Frequency?) {
        lastFrequency = freq
        loadRelevantTransactions()
    }
}
