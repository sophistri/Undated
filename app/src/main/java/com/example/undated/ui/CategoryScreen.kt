package com.example.undated.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.undated.data.Category
import com.example.undated.data.Task

private const val UNCATEGORIZED = -1L

private data class Section(
    val id: Long,
    val name: String,
    val color: Color?,
    val tasks: List<Task>
)

@Composable
fun CategoryScreen(viewModel: TaskViewModel) {
    val allTasks by viewModel.tasks.collectAsStateWithLifecycle()
    val tasks = allTasks.filter { it.eventId == null }
    val myEvents by viewModel.myEvents.collectAsStateWithLifecycle()
    val eventsWithSubtasks = myEvents.filter { e -> allTasks.any { it.eventId == e.id } }
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var collapsed by remember { mutableStateOf(setOf<Long>()) }

    var showNew by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Category?>(null) }

    val ink = MaterialTheme.colorScheme.outline

    val grouped = tasks.groupBy { it.categoryId ?: UNCATEGORIZED }
    val sections = categories.map {
        Section(it.id, it.name, Color(it.color), grouped[it.id].orEmpty())
    } + Section(UNCATEGORIZED, "Uncategorized", null, grouped[UNCATEGORIZED].orEmpty())

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {

        item(key = "new-category") {
            OutlinedButton(
                onClick = { showNew = true },
                shape = RoundedCornerShape(4.dp)
            ) { Text("+ NEW CATEGORY") }
        }

        sections.forEach { section ->
            val isCollapsed = section.id in collapsed

            item(key = "header-${section.id}") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            collapsed = if (isCollapsed) collapsed - section.id
                            else collapsed + section.id
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCollapsed) "[+]" else "[-]",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.width(8.dp))
                    section.color?.let {
                        ColorDot(it)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = section.name,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    val open = section.tasks.count { !it.isDone }
                    Text(
                        text = "$open open / ${section.tasks.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (section.id != UNCATEGORIZED) {
                        TextButton(onClick = {
                            toDelete = categories.firstOrNull { it.id == section.id }
                        }) { Text("Del") }
                    }
                }
                HorizontalDivider(thickness = 2.dp, color = ink)
            }

            if (!isCollapsed) {
                val sectionEvents = eventsWithSubtasks.filter {
                    (it.categoryId ?: UNCATEGORIZED) == section.id
                }
                items(sectionEvents, key = { "event-${it.id}" }) { event ->
                    EventCardFor(event, allTasks, categories, viewModel)
                }
                items(section.tasks, key = { "task-${it.id}" }) { task ->
                    TaskRow(
                        task = task,
                        color = task.color?.let { Color(it) } ?: section.color,
                        onToggle = { viewModel.toggleDone(task) },
                        onEdit = { viewModel.startEdit(task) },
                        onDelete = { viewModel.delete(task) }
                    )
                    HorizontalDivider(color = ink.copy(alpha = 0.3f))
                }
            }
        }
    }

    if (showNew) {
        NewCategoryDialog(
            onDismiss = { showNew = false },
            onCreate = { name, color ->
                viewModel.addCategory(name, color)
                showNew = false
            }
        )
    }

    toDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete \"${category.name}\"?") },
            text = { Text("Its tasks are kept and become uncategorized.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(category)
                    toDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
        )
    }

}