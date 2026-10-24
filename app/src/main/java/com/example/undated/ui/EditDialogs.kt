package com.example.undated.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.undated.data.Category
import com.example.undated.data.Event
import com.example.undated.data.Task
import java.time.LocalDate

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
    )
}

@Composable
private fun CategoryChipRow(
    categories: List<Category>,
    selected: Long?,
    onSelect: (Long?) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { c ->
            FilterChip(
                selected = selected == c.id,
                onClick = { onSelect(if (selected == c.id) null else c.id) },
                label = { Text(c.name) },
                leadingIcon = { ColorDot(Color(c.color)) },
                shape = RoundedCornerShape(4.dp)
            )
        }
    }
}

@Composable
private fun ColorSwatchRow(selected: Int?, onSelect: (Int?) -> Unit) {
    val ink = MaterialTheme.colorScheme.outline
    val shape = RoundedCornerShape(4.dp)
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(if (selected == null) 3.dp else 1.dp, ink, shape)
                .clickable { onSelect(null) },
            contentAlignment = Alignment.Center
        ) { Text("A", style = MaterialTheme.typography.labelLarge) }

        CATEGORY_COLORS.forEach { c ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(c), shape)
                    .border(if (selected == c) 3.dp else 1.dp, ink, shape)
                    .clickable { onSelect(c) }
            )
        }
    }
}

@Composable
fun EditTaskDialog(
    task: Task,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var categoryId by remember { mutableStateOf(task.categoryId) }
    var priority by remember { mutableIntStateOf(task.priority) }
    var dueStart by remember { mutableStateOf(task.dueStart) }
    var dueEnd by remember { mutableStateOf(task.dueEnd) }
    var color by remember { mutableStateOf(task.color) }
    var showPicker by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(4.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit task") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    shape = shape
                )

                FieldLabel("CATEGORY")
                CategoryChipRow(categories, categoryId) { categoryId = it }

                FieldLabel("PRIORITY")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("None", "Low", "Med", "High").forEachIndexed { level, label ->
                        FilterChip(
                            selected = priority == level,
                            onClick = { priority = level },
                            label = { Text(label) },
                            shape = shape
                        )
                    }
                }

                FieldLabel("DUE DATE")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { showPicker = true }, shape = shape) {
                        Text(formatDue(dueStart, dueEnd) ?: "No date")
                    }
                    if (dueStart != null) {
                        TextButton(onClick = { dueStart = null; dueEnd = null }) { Text("Clear") }
                    }
                }

                FieldLabel("COLOR (A = AUTO)")
                ColorSwatchRow(color) { color = it }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(
                        task.copy(
                            title = title.trim(),
                            categoryId = categoryId,
                            priority = priority,
                            dueStart = dueStart,
                            dueEnd = dueEnd,
                            color = color
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showPicker) {
        DueDatePickerDialog(
            initialStart = dueStart,
            initialEnd = dueEnd,
            onDismiss = { showPicker = false },
            onConfirm = { s, e ->
                dueStart = s
                dueEnd = e
                showPicker = false
            }
        )
    }
}


@Composable
fun EditEventDialog(
    event: Event,
    categories: List<Category>,
    subtasks: List<Task>,
    onDismiss: () -> Unit,
    onSave: (Event) -> Unit,
    onAddSubtask: (String) -> Unit,
    onDeleteSubtask: (Task) -> Unit
) {
    var title by remember { mutableStateOf(event.title) }
    var categoryId by remember { mutableStateOf(event.categoryId) }
    var start by remember { mutableLongStateOf(event.startDay) }
    var end by remember { mutableLongStateOf(event.endDay) }
    var color by remember { mutableStateOf(event.color) }
    var showPicker by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(4.dp)
    var newSubtask by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit event") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    shape = shape
                )

                FieldLabel("DATES")
                OutlinedButton(onClick = { showPicker = true }, shape = shape) {
                    Text(formatDue(start, end) ?: "Pick dates")
                }

                FieldLabel("CATEGORY")
                CategoryChipRow(categories, categoryId) { categoryId = it }

                FieldLabel("COLOR (A = AUTO)")
                ColorSwatchRow(color) { color = it }

                FieldLabel("SUBTASKS")
                subtasks.forEach { task ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = task.title,
                            modifier = Modifier.weight(1f),
                            textDecoration = if (task.isDone) TextDecoration.LineThrough else null
                        )
                        TextButton(onClick = { onDeleteSubtask(task) }) { Text("x") }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newSubtask,
                        onValueChange = { newSubtask = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Add module / subtask") },
                        singleLine = true,
                        shape = shape
                    )
                    TextButton(
                        enabled = newSubtask.isNotBlank(),
                        onClick = {
                            onAddSubtask(newSubtask)
                            newSubtask = ""
                        }
                    ) { Text("ADD") }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    if (newSubtask.isNotBlank()) onAddSubtask(newSubtask)
                    onSave(
                        event.copy(
                            title = title.trim(),
                            startDay = start,
                            endDay = end,
                            categoryId = categoryId,
                            color = color
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showPicker) {
        DueDatePickerDialog(
            initialStart = start,
            initialEnd = end,
            onDismiss = { showPicker = false },
            onConfirm = { s, e ->
                start = s
                end = e
                showPicker = false
            }
        )
    }
}

@Composable
fun NewTaskDialog(
    categories: List<Category>,
    defaultDay: LocalDate?,
    onDismiss: () -> Unit,
    onCreate: (
        title: String,
        categoryId: Long?,
        priority: Int,
        dueStart: Long?,
        dueEnd: Long?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var priority by remember { mutableIntStateOf(0) }
    var dueStart by remember { mutableStateOf(defaultDay?.toEpochDay()) }
    var dueEnd by remember { mutableStateOf(defaultDay?.toEpochDay()) }
    var showPicker by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(4.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New task") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Revise chapter 3") },
                    singleLine = true,
                    shape = shape
                )

                FieldLabel("CATEGORY")
                CategoryChipRow(categories, categoryId) { categoryId = it }

                FieldLabel("PRIORITY")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("None", "Low", "Med", "High").forEachIndexed { level, label ->
                        FilterChip(
                            selected = priority == level,
                            onClick = { priority = level },
                            label = { Text(label) },
                            shape = shape
                        )
                    }
                }

                FieldLabel("DUE DATE (OPTIONAL)")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { showPicker = true }, shape = shape) {
                        Text(formatDue(dueStart, dueEnd) ?: "No date")
                    }
                    if (dueStart != null) {
                        TextButton(onClick = { dueStart = null; dueEnd = null }) { Text("Clear") }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { onCreate(title.trim(), categoryId, priority, dueStart, dueEnd) }
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showPicker) {
        DueDatePickerDialog(
            initialStart = dueStart,
            initialEnd = dueEnd,
            onDismiss = { showPicker = false },
            onConfirm = { s, e ->
                dueStart = s
                dueEnd = e
                showPicker = false
            }
        )
    }
}