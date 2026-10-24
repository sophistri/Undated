package com.example.undated.ui

import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val MILLIS_PER_DAY = 86_400_000L

/** Turns two stored epoch days into text like "Oct 8" or "Oct 8 - Oct 14". */
fun formatDue(start: Long?, end: Long?): String? {
    if (start == null) return null
    val fmt = DateTimeFormatter.ofPattern("MMM d")
    val s = LocalDate.ofEpochDay(start)
    val e = LocalDate.ofEpochDay(end ?: start)
    return if (s == e) s.format(fmt) else "${s.format(fmt)} - ${e.format(fmt)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueDatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (start: Long, end: Long) -> Unit,
    initialStart: Long? = null,
    initialEnd: Long? = null
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStart?.times(MILLIS_PER_DAY),
        initialSelectedEndDateMillis = initialEnd?.times(MILLIS_PER_DAY)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedStartDateMillis != null,
                onClick = {
                    val start = state.selectedStartDateMillis ?: return@TextButton
                    val end = state.selectedEndDateMillis ?: start
                    // The picker gives UTC-midnight milliseconds, so dividing by one
                    // day's worth of milliseconds gives the epoch day directly.
                    onConfirm(start / MILLIS_PER_DAY, end / MILLIS_PER_DAY)
                }
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier.height(500.dp)
        )
    }
}