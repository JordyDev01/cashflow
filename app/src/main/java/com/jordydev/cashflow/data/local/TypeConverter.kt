package com.jordydev.cashflow.data.local

import androidx.room.TypeConverter
import com.jordydev.cashflow.util.Frequency

object Converters  {
    @TypeConverter
    fun fromFrequency(freq: Frequency): String = freq.name

    @TypeConverter
    fun toFrequency(value: String): Frequency = Frequency.valueOf(value)
}
