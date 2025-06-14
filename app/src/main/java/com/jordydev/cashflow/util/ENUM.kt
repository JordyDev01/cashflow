package com.jordydev.cashflow.util

enum class Frequency {
    ALL, ONCE, DAILY, WEEKLY, BIWEEKLY, MONTHLY;


    fun displayName(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}

enum class TransactionType {
    INCOME, EXPENSE;

//    fun displayName(): String {
//        return name.lowercase().replaceFirstChar { it.uppercase() }
//    }
}