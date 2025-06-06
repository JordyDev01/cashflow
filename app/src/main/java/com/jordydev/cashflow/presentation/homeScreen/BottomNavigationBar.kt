package com.jordydev.cashflow.presentation.homeScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController

//@Composable
//fun BottomNavigationBar(
//    onSelect : () -> Unit
//) {
//    val items = listOf("Daily", "Weekly", "Monthly")
//    val selectedItem = remember { mutableStateOf(1) } // Weekly as default
//
//    NavigationBar {
//        items.forEachIndexed { index, item ->
//            NavigationBarItem(
//                icon = { Icon(Icons.Default.DateRange, contentDescription = item) },
//                label = { Text(item) },
//                selected = selectedItem.value == index,
//                onClick = {
//                    selectedItem.value = index
//                }
//            )
//        }
//    }
//}