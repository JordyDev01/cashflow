package com.jordydev.cashflow.presentation.addtransaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.jordydev.cashflow.data.local.TransactionEntity
import com.jordydev.cashflow.presentation.homeScreen.TransactionViewModel
import com.jordydev.cashflow.util.Frequency
import com.jordydev.cashflow.util.TransactionType
import java.time.LocalDate
import java.util.Calendar
import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTransactionScreen(navController: NavHostController, viewModel: TransactionViewModel) {

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var id by remember { mutableStateOf<Int?>(null) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.INCOME) }
    var frequency by remember { mutableStateOf(Frequency.MONTHLY) }
    var date by remember { mutableStateOf(LocalDate.now()) }

    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val typeOptions = TransactionType.entries.toList()
    val frequencyOptions = Frequency.entries.toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Add Transaction", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                titleError = false
            },
            label = { Text("Title") },
            isError = titleError,
            modifier = Modifier.fillMaxWidth()
        )
        if (titleError) {
            Text("Title cannot be empty", color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                amountError = false
            },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = amountError,
            modifier = Modifier.fillMaxWidth()
        )
        if (amountError) {
            Text("Enter a valid amount", color = MaterialTheme.colorScheme.error)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    DatePickerDialog(
                        context,
                        { _, selectedYear, selectedMonth, selectedDay ->
                            date = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                        },
                        date.year,
                        date.monthValue - 1,
                        date.dayOfMonth
                    ).show()
                }
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Date: ${date}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date"
                )
            }
        }

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
                val parsedAmount = amount.toDoubleOrNull()
                val isTitleValid = title.isNotBlank()
                val isAmountValid = parsedAmount != null && parsedAmount > 0.0

                if (!isTitleValid) titleError = true
                if (!isAmountValid) amountError = true

                if (isTitleValid && isAmountValid) {
                    val transaction = TransactionEntity(
                        id = id ?: 0,
                        title = title,
                        amount = parsedAmount,
                        type = type,
                        frequency = frequency,
                        date = date.toString()
                    )
                    viewModel.addTransaction(transaction)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save")
        }
    }
}
