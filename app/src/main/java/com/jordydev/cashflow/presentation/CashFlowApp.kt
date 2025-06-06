package com.jordydev.cashflow.presentation

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jordydev.cashflow.data.local.TransactionEntity
import com.jordydev.cashflow.presentation.addtransaction.AddTransactionScreen
import com.jordydev.cashflow.presentation.edit_transaction.EditTransactionScreen
import com.jordydev.cashflow.presentation.homeScreen.HomeScreen
import com.jordydev.cashflow.presentation.homeScreen.TransactionViewModel
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun CashFlowApp(
    viewModel: TransactionViewModel = viewModel(),
    onExportClick: () -> Unit,
    onAboutClick: () -> Unit
){

    var showDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    val navController = rememberNavController()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("CashFlow") },
            actions = {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export to PDF") },
                            onClick = {
                                expanded = false
                                showDatePicker = true
                                onExportClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("About Us") },
                            onClick = {
                                expanded = false
                                onAboutClick()
                            }
                        )
                    }
                }
            })},
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_transaction") }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                HomeScreen(viewModel,
                    navController = navController)
            }
            }
            composable("add_transaction") { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AddTransactionScreen(navController, viewModel)
            }
            }

            composable("edit_transaction/{transactionId}") { backStackEntry ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val transactionId = backStackEntry.arguments?.getString("transactionId")?.toIntOrNull()

                    if (transactionId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        EditTransactionScreen(
                            navController = navController,
                            viewModel = viewModel,
                            transactionId = transactionId
                        )
                    } else {
                        Text("Invalid transaction ID")
                    }
                }
            }
        }
    }
}