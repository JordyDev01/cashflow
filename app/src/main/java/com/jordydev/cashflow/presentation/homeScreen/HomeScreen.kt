package com.jordydev.cashflow.presentation.homeScreen

import TransactionItem
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.jordydev.cashflow.data.local.TransactionEntity
import com.jordydev.cashflow.presentation.addtransaction.DropdownSelector
import com.jordydev.cashflow.util.Frequency


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: TransactionViewModel,
    navController: NavHostController
) {
    val transactions by viewModel.transactionState.collectAsState()
    val frequencies = Frequency.entries.toList()
    val dateRanges = listOf("Last 2 Weeks", "Last 3 Months", "Last 6 Months", "Last 12 Months")

    var selectedFrequency by rememberSaveable { mutableStateOf<Frequency?>(null) }
    var selectedDateRange by rememberSaveable { mutableStateOf("Last 2 Weeks") }

    val income by viewModel.totalIncome.collectAsState()
    val expenses by viewModel.totalExpenses.collectAsState()
    val balance by viewModel.balance.collectAsState()

    val animatedIncome by animateFloatAsState(
        targetValue = income.toFloat(),
        animationSpec = tween(durationMillis = 600)
    )
    val animatedExpenses by animateFloatAsState(
        targetValue = expenses.toFloat(),
        animationSpec = tween(durationMillis = 600)
    )
    val animatedBalance by animateFloatAsState(
        targetValue = balance.toFloat(),
        animationSpec = tween(durationMillis = 600)
    )

    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    LaunchedEffect(selectedDateRange, selectedFrequency) {
        viewModel.loadRelevantTransactions(
            rangeLabel = selectedDateRange,
            frequency = selectedFrequency
        )
    }

    Column(modifier = Modifier
        .padding(16.dp)) {

        // Header Info
        Text("Balance", style = MaterialTheme.typography.headlineSmall)
        Text(
            "$${String.format("%.2f", animatedBalance)}",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Income", style = MaterialTheme.typography.labelMedium)
                Text("$${String.format("%.2f", animatedIncome)}", color = Color.Green)
            }
            Column {
                Text("Expenses", style = MaterialTheme.typography.labelMedium)
                Text("$${String.format("%.2f", animatedExpenses)}", color = Color.Red)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Toggle for Future Transactions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
//            Switch(
//                checked = showFutureTransactions,
//                onCheckedChange = { showFutureTransactions = it }
//            )
//            Text("Show Future Transactions", modifier = Modifier.padding(start = 8.dp))
        }

        // Dropdown Filters
            DropdownSelector(
                label = "Filter by Date Range",
                options = dateRanges,
                selected = selectedDateRange
            ) { selected ->
                selectedDateRange = selected
                viewModel.setSelectedDateRange(selected)
            }

            DropdownSelector(
                label = "Filter by Frequency",
                options = frequencies.map {
                    it.name.lowercase().replaceFirstChar(Char::uppercase)
                }.toMutableList(),
                selected = selectedFrequency?.name?.lowercase()?.replaceFirstChar(Char::uppercase)
                    ?: "All"
            ) { selected ->
                selectedFrequency = if (selected == "All") null
                else Frequency.entries.firstOrNull { it.name.equals(selected, ignoreCase = true) }
                viewModel.setSelectedFrequency(selectedFrequency)
            }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Transactions", style = MaterialTheme.typography.titleMedium)

        // Delete Dialog
        if (showDeleteDialog && transactionToDelete != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Transaction") },
                text = { Text("Are you sure you want to delete \"${transactionToDelete!!.title}\"?") },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            viewModel.deleteTransaction(transactionToDelete!!)
                            showDeleteDialog = false
                            transactionToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showDeleteDialog = false
                            transactionToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//            // Transaction List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 80.dp),
                contentPadding = PaddingValues(bottom = 60.dp),
            ) {
                itemsIndexed(transactions, key = { _, txn -> txn.id }) { index, txn ->
                    val visible = remember(index) { mutableStateOf(false) }

                    LaunchedEffect(index) {
                        kotlinx.coroutines.delay(index * 70L)
                        visible.value = true
                    }
                        TransactionItem(
                            txn,
                            onEdit = {
                                navController.navigate("edit_transaction/${txn.id}")
                            },
                            onDelete = {
                                transactionToDelete = txn
                                showDeleteDialog = true
                            }
                        )


                }
            }
        }
//    }
}


