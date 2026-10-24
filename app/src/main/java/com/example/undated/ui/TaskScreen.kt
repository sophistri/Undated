package com.example.undated.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.undated.data.Task

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val allTasks by viewModel.tasks.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val myEvents by viewModel.myEvents.collectAsStateWithLifecycle()
    var showNewTask by remember { mutableStateOf(false) }

    val tasks = allTasks.filter { it.eventId == null }
    val eventsWithSubtasks = myEvents.filter { e -> allTasks.any { it.eventId == e.id } }
    val categoryById = categories.associateBy { it.id }
    val ink = MaterialTheme.colorScheme.outline

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedButton(
            onClick = { showNewTask = true },
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) { Text("+ NEW TASK") }

        Spacer(Modifier.height(12.dp))

        LazyColumn {
            items(eventsWithSubtasks, key = { "event-${it.id}" }) { event ->
                EventCardFor(event, allTasks, categories, viewModel)
            }
            items(tasks, key = { it.id }) { task ->
                val color = (task.color ?: task.categoryId?.let { categoryById[it]?.color })
                    ?.let { Color(it) }

                TaskRow(
                    task = task,
                    color = color,
                    onToggle = { viewModel.toggleDone(task) },
                    onEdit = { viewModel.startEdit(task) },
                    onDelete = { viewModel.delete(task) }
                )
                HorizontalDivider(color = ink.copy(alpha = 0.3f))
            }
        }
    }

    if (showNewTask) {
        NewTaskDialog(
            categories = categories,
            defaultDay = null,
            onDismiss = { showNewTask = false },
            onCreate = { title, categoryId, priority, start, end ->
                viewModel.addTask(title, categoryId, priority, start, end)
                showNewTask = false
            }
        )
    }
}

@Composable
fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color, RoundedCornerShape(2.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
    )
}

@Composable
fun TaskRow(
    task: Task,
    color: Color?,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = task.isDone, onCheckedChange = { onToggle() })
        if (color != null) {
            ColorDot(color)
            Spacer(Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f).clickable(onClick = onEdit)) {
            Text(
                text = task.title,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else null
            )
            formatDue(task.dueStart, task.dueEnd)?.let { due ->
                Text(
                    text = due,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (task.priority > 0) {
            Text(
                text = "!".repeat(task.priority),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
        }
        TextButton(onClick = onDelete) { Text("Delete") }
    }
}