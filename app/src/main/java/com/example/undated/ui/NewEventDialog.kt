package com.example.undated.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.undated.data.Category
import java.time.LocalDate

@Composable
fun NewEventDialog(
    categories: List<Category>,
    defaultDay: LocalDate,
    onDismiss: () -> Unit,
    onCreate: (title: String, startDay: Long, endDay: Long, categoryId: Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var start by remember { mutableLongStateOf(defaultDay.toEpochDay()) }
    var end by remember { mutableLongStateOf(defaultDay.toEpochDay()) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var showPicker by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(4.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New event") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Physics exam") },
                    singleLine = true,
                    shape = shape
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { showPicker = true }, shape = shape) {
                    Text(formatDue(start, end) ?: "Pick dates")
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { c ->
                        FilterChip(
                            selected = categoryId == c.id,
                            onClick = { categoryId = if (categoryId == c.id) null else c.id },
                            label = { Text(c.name) },
                            leadingIcon = { ColorDot(Color(c.color)) },
                            shape = shape
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { onCreate(title, start, end, categoryId) }
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showPicker) {
        DueDatePickerDialog(
            onDismiss = { showPicker = false },
            onConfirm = { s, e ->
                start = s
                end = e
                showPicker = false
            }
        )
    }
}