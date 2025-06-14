package com.jordydev.cashflow

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.jordydev.cashflow.data.local.CashFlowDatabase
import com.jordydev.cashflow.data.repository.TransactionRepository
import com.jordydev.cashflow.presentation.CashFlowApp
import com.jordydev.cashflow.presentation.homeScreen.HomeScreen
import com.jordydev.cashflow.presentation.homeScreen.TransactionViewModel
import com.jordydev.cashflow.ui.theme.CashflowTheme
import com.jordydev.cashflow.util.SplashScreen
import com.jordydev.cashflow.util.exportTransactionsToPdf
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: TransactionViewModel
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CRASH_HANDLER", "Uncaught exception: ${throwable.message}", throwable)
        }
        enableEdgeToEdge()
        val db = CashFlowDatabase.getDatabase(applicationContext)
        val repository = TransactionRepository(db.transactionDao())

        // ✅ Manual ViewModel creation (no ViewModelProvider)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewModel = TransactionViewModel(repository)
        }
        setContent {
            CashflowTheme {
                    CashFlowApp(
                        viewModel = viewModel,
                        onExportClick = {
                            lifecycleScope.launch {
                                val transactions = viewModel.transactionState.value
                                exportTransactionsToPdf(this@MainActivity, transactions)
                            }
                        },
                        onAboutClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://apex-code.netlify.app/"))
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }


