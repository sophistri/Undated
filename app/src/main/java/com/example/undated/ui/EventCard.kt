package com.example.undated.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.undated.data.Category
import com.example.undated.data.Event
import com.example.undated.data.Task

/** Event colour: its own override, else its category's colour, else a theme colour. */
fun eventColorOf(event: Event, categories: List<Category>, fallback: Color): Color =
    (event.color ?: event.categoryId?.let { id -> categories.firstOrNull { it.id == id }?.color })
        ?.let { Color(it) } ?: fallback

/** One-line call for any screen: wires the card to the ViewModel. */
@Composable
fun EventCardFor(
    event: Event,
    allTasks: List<Task>,
    categories: List<Category>,
    viewModel: TaskViewModel
) {
    EventCard(
        event = event,
        subtasks = allTasks.filter { it.eventId == event.id }.sortedBy { it.id },
        color = eventColorOf(event, categories, MaterialTheme.colorScheme.tertiary),

        onToggle = { viewModel.toggleDone(it) },
        onDeleteSubtask = { viewModel.delete(it) },
        onDeleteEvent = { viewModel.deleteEvent(event) },
        onEdit = { viewModel.startEditEvent(event) },
        onEditSubtask = { viewModel.startEdit(it) }
    )
}

@Composable
fun EventCard(
    event: Event,
    subtasks: List<Task>,
    color: Color,

    onToggle: (Task) -> Unit,
    onDeleteSubtask: (Task) -> Unit,
    onDeleteEvent: () -> Unit,
    onEdit: () -> Unit,
    onEditSubtask: (Task) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(4.dp)
    var expanded by rememberSaveable(event.id) { mutableStateOf(false) }

    var confirmDelete by remember { mutableStateOf(false) }

    val done = subtasks.count { it.isDone }
    val fraction = if (subtasks.isEmpty()) 0f else done.toFloat() / subtasks.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(colors.surfaceVariant, shape)
            .border(2.dp, colors.outline, shape)
    ) {
        // Header: always visible, tap to open or close.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (expanded) "[-]" else "[+]",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(12.dp).background(color, CircleShape))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, fontWeight = FontWeight.Bold)
                Text(
                    text = formatDue(event.startDay, event.endDay) ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary
                )
            }
            if (subtasks.isNotEmpty()) {
                Text("$done/${subtasks.size}", style = MaterialTheme.typography.labelLarge)
            }
        }

        if (subtasks.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                    .fillMaxWidth()
                    .height(10.dp)
                    .border(2.dp, colors.outline, RoundedCornerShape(2.dp))
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .background(color)
                )
            }
        }

        // Body: only exists while open.
        if (expanded) {
            Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
                Row {
                    TextButton(onClick = onEdit) { Text("Edit") }
                    TextButton(onClick = { confirmDelete = true }) { Text("Del") }
                }

                subtasks.forEach { task ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = task.isDone, onCheckedChange = { onToggle(task) })
                        Text(
                            text = task.title,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onEditSubtask(task) },
                            textDecoration = if (task.isDone) TextDecoration.LineThrough else null
                        )
                        TextButton(onClick = { onDeleteSubtask(task) }) { Text("x") }
                    }
                }

                if (subtasks.isEmpty()) {
                    Text(
                        text = "No subtasks yet. Tap Edit to add some.",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete \"${event.title}\"?") },
            text = { Text("Its subtasks will be deleted too.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDeleteEvent()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}