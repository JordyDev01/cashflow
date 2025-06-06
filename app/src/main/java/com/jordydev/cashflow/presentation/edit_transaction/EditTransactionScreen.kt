package com.jordydev.cashflow.presentation.edit_transaction

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.jordydev.cashflow.presentation.addtransaction.DropdownSelector
import com.jordydev.cashflow.presentation.homeScreen.TransactionViewModel
import com.jordydev.cashflow.util.Frequency
import com.jordydev.cashflow.util.TransactionType
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditTransactionScreen(
    navController: NavHostController,
    viewModel: TransactionViewModel,
    transactionId: Int
) {
    val transaction = viewModel.transactionState.collectAsState().value.find { it.id == transactionId }

    if (transaction == null) {
        Text("Transaction not found")
        return
    }

    var title by remember { mutableStateOf(transaction.title) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var type by remember { mutableStateOf(transaction.type) }
    var frequency by remember { mutableStateOf(transaction.frequency) }
    var date by remember { mutableStateOf(LocalDate.parse(transaction.date)) }

    val typeOptions = TransactionType.entries.toList()
    val frequencyOptions = Frequency.entries.toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Edit Transaction", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        DropdownSelector(
            label = "Type",
            options = typeOptions.map { it.name.lowercase().replaceFirstChar(Char::uppercase) },
            selected = type.name.lowercase().replaceFirstChar(Char::uppercase)
        ) { selected ->
            type = typeOptions.first { it.name.equals(selected, ignoreCase = true) }
        }

        DropdownSelector(
            label = "Frequency",
            options = frequencyOptions.map { it.name.lowercase().replaceFirstChar(Char::uppercase) },
            selected = frequency.name.lowercase().replaceFirstChar(Char::uppercase)
        ) { selected ->
            frequency = frequencyOptions.first { it.name.equals(selected, ignoreCase = true) }
        }

        Button(
            onClick = {
                val updatedTxn = transaction.copy(
                    title = title,
                    amount = amount.toDoubleOrNull() ?: transaction.amount,
                    type = type,
                    frequency = frequency,
                    date = date.toString()
                )
                viewModel.updateTransaction(updatedTxn)
                navController.popBackStack()
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Update")
        }
    }
}

