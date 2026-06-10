package com.cs346.intellical.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

fun earliestSafeGoalDate(
    currentDate: LocalDate = LocalDate.now(),
    absoluteMinWeeks: Int
): LocalDate {
    return currentDate.plusWeeks(absoluteMinWeeks.coerceAtLeast(0).toLong())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeGoalDatePicker(absoluteMinWeeks: Int) {
    val earliestSafeDate = remember(absoluteMinWeeks) {
        earliestSafeGoalDate(absoluteMinWeeks = absoluteMinWeeks)
    }
    val formatter = remember {
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
    }
    val earliestSafeDateMillis = remember(earliestSafeDate) {
        earliestSafeDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    }
    val selectableDates = remember(earliestSafeDate) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val selectedDate = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                return !selectedDate.isBefore(earliestSafeDate)
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year >= earliestSafeDate.year
            }
        }
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = earliestSafeDateMillis,
        selectableDates = selectableDates
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DatePicker(state = datePickerState)
        Text(
            text = "Earliest safe date: ${earliestSafeDate.format(formatter)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
