package com.example.undated.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel


// Adding a tab later means adding one line here and one in the when.
enum class AppTab(val label: String) {
    HOME("HOME"),
    TASKS("TASKS"),
    CATEGORIES("CATS"),
    CALENDAR("CAL"),
    STATS("STATS")
}

@Composable
fun AppShell(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel() // same for all tabs so that they all see the same data
) {
    var tab by rememberSaveable { mutableStateOf(AppTab.HOME) }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val editingTask by viewModel.editingTask.collectAsStateWithLifecycle()
    val editingEvent by viewModel.editingEvent.collectAsStateWithLifecycle()

    val allTasks by viewModel.tasks.collectAsStateWithLifecycle()

    RetroWindow(
        title = "undated.exe",
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        RetroTabBar(selected = tab, onSelect = { tab = it })
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.outline)

        Box(modifier = Modifier.weight(1f)) {
            // picks which screen to draw. Only the selected screen exists at a time.
            when (tab) {
                AppTab.HOME -> ScheduleScreen(viewModel)
                AppTab.TASKS -> TaskScreen(viewModel)
                AppTab.CATEGORIES -> CategoryScreen(viewModel)
                AppTab.CALENDAR -> CalendarScreen(viewModel)
                AppTab.STATS -> StatsScreen(viewModel)
            }
        }
    }

    editingTask?.let { task ->
        EditTaskDialog(
            task = task,
            categories = categories,
            onDismiss = viewModel::stopEdit,
            onSave = viewModel::updateTask
        )
    }

    editingEvent?.let { event ->
        EditEventDialog(
            event = event,
            categories = categories,
            subtasks = allTasks.filter { it.eventId == event.id }.sortedBy { it.id },
            onDismiss = viewModel::stopEdit,
            onSave = viewModel::updateEvent,
            onAddSubtask = { viewModel.addSubtask(event.id, it) },
            onDeleteSubtask = { viewModel.delete(it) }
        )
    }
}

@Composable
private fun RetroTabBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth().background(colors.surfaceVariant)) {
        AppTab.entries.forEach { t ->
            val isSelected = t == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (isSelected) colors.secondary else Color.Transparent)
                    .clickable { onSelect(t) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = t.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) colors.onSecondary else colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ComingSoon(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PixelSunflower(modifier = Modifier.size(64.dp), spinning = false)
            Spacer(Modifier.height(12.dp))
            Text(text = "$name: coming soon", style = MaterialTheme.typography.labelLarge)
        }
    }
}