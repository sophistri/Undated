package com.example.undated.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.undated.data.DeviceEvent
import com.example.undated.data.Event
import com.example.undated.data.Task
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val WEEKDAYS = listOf("S", "M", "T", "W", "T", "F", "S")

@Composable
fun CalendarScreen(viewModel: TaskViewModel) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val myEvents by viewModel.myEvents.collectAsStateWithLifecycle()
    var showNewEvent by remember { mutableStateOf(false) }

    val importedKeys = myEvents.mapNotNull { e ->
        val id = e.sourceEventId
        val begin = e.sourceBegin
        if (id != null && begin != null) id to begin else null
    }.toSet()
    val visibleDeviceEvents = events.filter { (it.id to it.startMillis) !in importedKeys }

    val today = remember { LocalDate.now() }
    var month by rememberSaveable { mutableStateOf(YearMonth.from(today)) }
    var selected by rememberSaveable { mutableStateOf(today) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(month, hasPermission) {
        if (hasPermission) viewModel.loadEvents(month.atDay(1), month.atEndOfMonth())
    }

    val colors = MaterialTheme.colorScheme
    val categoryById = categories.associateBy { it.id }

    fun tasksOn(date: LocalDate): List<Task> {
        val day = date.toEpochDay()
        return tasks.filter { t ->
            val start = t.dueStart ?: return@filter false
            val end = t.dueEnd ?: start
            day in start..end
        }
    }

    fun eventsOn(date: LocalDate): List<DeviceEvent> {
        val day = date.toEpochDay()
        return visibleDeviceEvents.filter { day in it.startDay..it.endDay }
    }

    fun colorFor(task: Task): Color? =
        (task.color ?: task.categoryId?.let { categoryById[it]?.color })?.let { Color(it) }

    fun myEventsOn(date: LocalDate): List<Event> {
        val day = date.toEpochDay()
        return myEvents.filter { day in it.startDay..it.endDay }
    }

    fun eventColor(e: Event): Color =
        (e.color ?: e.categoryId?.let { categoryById[it]?.color })?.let { Color(it) }
            ?: colors.tertiary

    val offset = month.atDay(1).dayOfWeek.value % 7
    val cells: List<LocalDate?> =
        List(offset) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    val weeks = (cells + List((7 - cells.size % 7) % 7) { null }).chunked(7)

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showNewEvent = true },
                shape = RoundedCornerShape(4.dp)
            ) { Text("+ EVENT") }

            if (!hasPermission) {
                OutlinedButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.READ_CALENDAR) },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("IMPORT EVENTS") }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { month = month.minusMonths(1) }) { Text("<") }
            Text(
                text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        month = YearMonth.from(today)
                        selected = today
                    }
            )
            TextButton(onClick = { month = month.plusMonths(1) }) { Text(">") }
        }

        Row {
            WEEKDAYS.forEach { d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        weeks.forEach { week ->
            Row {
                week.forEach { date ->
                    if (date == null) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        DayCell(
                            day = date.dayOfMonth,
                            isToday = date == today,
                            isSelected = date == selected,
                            taskDots = tasksOn(date)
                                .filter { !it.isDone }
                                .map { colorFor(it) ?: colors.primary },
                            eventDots = myEventsOn(date).map { eventColor(it) } + eventsOn(date).map { Color(it.color) },
                            onClick = { selected = date },
                            modifier = Modifier.weight(1f).aspectRatio(1f)
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 2.dp,
            color = colors.outline
        )

        Text(
            text = selected.format(DateTimeFormatter.ofPattern("EEE, MMM d")).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        val dayTasks = tasksOn(selected)
        val dayEvents = eventsOn(selected)
        val dayMyEvents = myEventsOn(selected)

        if (dayTasks.isEmpty() && dayEvents.isEmpty() && dayMyEvents.isEmpty()) {
            Text("Nothing on this day")
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(dayMyEvents, key = { "myevent-${it.id}" }) { event ->
                    EventCardFor(event, tasks, categories, viewModel)
                }
                items(dayEvents, key = { "event-${it.id}-${it.startMillis}" }) { event ->
                    EventRow(
                        event = event,
                        onAddAsEvent = { viewModel.importEvent(event) }
                    )
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.3f))
                }
                items(dayTasks, key = { "task-${it.id}" }) { task ->
                    TaskRow(
                        task = task,
                        color = colorFor(task),
                        onToggle = { viewModel.toggleDone(task) },
                        onEdit = { viewModel.startEdit(task) },
                        onDelete = { viewModel.delete(task) }
                    )
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.3f))
                }
            }
        }

        if (showNewEvent) {
            NewEventDialog(
                categories = categories,
                defaultDay = selected,
                onDismiss = { showNewEvent = false },
                onCreate = { title, start, end, categoryId ->
                    viewModel.addEvent(title, start, end, categoryId)
                    showNewEvent = false
                }
            )
        }
    }
}

@Composable
private fun EventRow(event: DeviceEvent, onAddAsEvent: () -> Unit) {
    val zone = ZoneId.systemDefault()
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    val label = if (event.allDay) {
        "all day"
    } else {
        val from = Instant.ofEpochMilli(event.startMillis).atZone(zone).format(timeFormat)
        val to = Instant.ofEpochMilli(event.endMillis).atZone(zone).format(timeFormat)
        "$from - $to"
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).background(Color(event.color), CircleShape))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = event.title)
            Text(
                text = "$label | ${event.calendarName}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        TextButton(onClick = onAddAsEvent) { Text("+ Event") }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    taskDots: List<Color>,
    eventDots: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier = modifier
            .padding(2.dp)
            .background(if (isSelected) colors.primaryContainer else Color.Transparent, shape)
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = if (isToday) colors.primary else colors.outline.copy(alpha = 0.25f),
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) colors.onPrimaryContainer else colors.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                taskDots.take(2).forEach { c ->
                    Box(Modifier.size(6.dp).background(c, RoundedCornerShape(1.dp)))
                }
                eventDots.take(2).forEach { c ->
                    Box(Modifier.size(6.dp).background(c, CircleShape))
                }
            }
        }
    }
}