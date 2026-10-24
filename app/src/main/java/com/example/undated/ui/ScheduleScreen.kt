package com.example.undated.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.undated.data.DeviceEvent
import com.example.undated.data.Event
import com.example.undated.data.Task
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CalendarPermission(val granted: Boolean, val request: () -> Unit)

@Composable
fun rememberCalendarPermission(): CalendarPermission {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted = it }
    return CalendarPermission(granted) { launcher.launch(Manifest.permission.READ_CALENDAR) }
}

private enum class Kind {
    ANYTIME_HEADER, ANYTIME_TASK, DAY_HEADER, EMPTY_DAY, OWN_EVENT, DEVICE_EVENT, TASK
}

private data class SchedItem(
    val key: String,
    val kind: Kind,
    val date: LocalDate,
    val task: Task? = null,
    val myEvent: Event? = null,
    val deviceEvent: DeviceEvent? = null
)

private const val HORIZON_DAYS = 90L

private val EMPTY_DAY_MESSAGES = listOf(
    "Enjoy your day!",
    "Go touch grass.",
    "Nothing to do. Suspicious.",
    "Enjoy that.",
    "Free as a bird.",
    "Do absolutely nothing.",
    "Take a nap, you earned it.",
    "Have the day you deserve.",
    "Go outside. Seriously.",
    "Have the day you deserve."
)

@Composable
fun ScheduleScreen(viewModel: TaskViewModel) {
    val allTasks by viewModel.tasks.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val myEvents by viewModel.myEvents.collectAsStateWithLifecycle()
    val deviceEvents by viewModel.events.collectAsStateWithLifecycle()
    val permission = rememberCalendarPermission()

    val colors = MaterialTheme.colorScheme
    val today = remember { LocalDate.now() }
    val todayDay = today.toEpochDay()

    var anytimeOpen by rememberSaveable { mutableStateOf(false) }
    var addTarget by remember { mutableStateOf(today) }
    var showChooser by remember { mutableStateOf(false) }
    var showNewEvent by remember { mutableStateOf(false) }
    var showNewTask by remember { mutableStateOf(false) }

    val sessionSeed = remember { System.nanoTime() }

    LaunchedEffect(permission.granted) {
        if (permission.granted) viewModel.loadEvents(today, today.plusDays(HORIZON_DAYS))
    }

    val categoryById = categories.associateBy { it.id }
    val importedKeys = myEvents.mapNotNull { e ->
        val id = e.sourceEventId
        val begin = e.sourceBegin
        if (id != null && begin != null) id to begin else null
    }.toSet()
    val visibleDevice = deviceEvents.filter { (it.id to it.startMillis) !in importedKeys }

    val standalone = allTasks.filter { it.eventId == null }
    val undated = standalone.filter { it.dueStart == null && !it.isDone }

    val schedule = buildList {
        add(SchedItem("anytime-header", Kind.ANYTIME_HEADER, today))
        if (anytimeOpen) {
            undated.forEach { add(SchedItem("anytime-${it.id}", Kind.ANYTIME_TASK, today, task = it)) }
        }

        for (offset in 0..HORIZON_DAYS) {
            val date = today.plusDays(offset)
            val day = date.toEpochDay()

            val dayEvents = myEvents.filter { day in it.startDay..it.endDay }
            val dayDevice = visibleDevice.filter { day in it.startDay..it.endDay }
            val dayTasks = standalone.filter { t ->
                val start = t.dueStart ?: return@filter false
                val end = t.dueEnd ?: start
                if (end >= todayDay) maxOf(start, todayDay) == day   // upcoming or ongoing
                else !t.isDone && day == todayDay                    // overdue shows today
            }

            val isEmpty = dayEvents.isEmpty() && dayDevice.isEmpty() && dayTasks.isEmpty()
            if (isEmpty && offset > 0) continue

            add(SchedItem("day-$day", Kind.DAY_HEADER, date))
            if (isEmpty) add(SchedItem("empty-$day", Kind.EMPTY_DAY, date))
            dayEvents.forEach { add(SchedItem("event-$day-${it.id}", Kind.OWN_EVENT, date, myEvent = it)) }
            dayDevice.forEach {
                add(SchedItem("device-$day-${it.id}-${it.startMillis}", Kind.DEVICE_EVENT, date, deviceEvent = it))
            }
            dayTasks.forEach { add(SchedItem("task-$day-${it.id}", Kind.TASK, date, task = it)) }
        }
    }

    // The "Add on <date>" pill follows the day at the top of the scroll position.
    val listState = rememberLazyListState()
    val currentSchedule by rememberUpdatedState(schedule)
    val addDate by remember {
        derivedStateOf {
            currentSchedule.getOrNull(listState.firstVisibleItemIndex)?.date ?: today
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
        ) {
            if (!permission.granted) {
                item(key = "permission") {
                    OutlinedButton(
                        onClick = permission.request,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("IMPORT GOOGLE CALENDAR EVENTS") }
                }
            }

            items(schedule, key = { it.key }) { item ->
                when (item.kind) {
                    Kind.ANYTIME_HEADER -> Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { anytimeOpen = !anytimeOpen }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (anytimeOpen) "[-]" else "[+]",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "ANYTIME",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${undated.size} open",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.primary
                            )
                        }
                        HorizontalDivider(thickness = 2.dp, color = colors.outline)
                    }

                    Kind.DAY_HEADER -> Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.date == today) {
                            Text(
                                text = "TODAY",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onPrimary,
                                modifier = Modifier
                                    .background(colors.primary, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = item.date.format(DateTimeFormatter.ofPattern("EEE, d MMM")),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Kind.EMPTY_DAY -> {
                        val message = remember(item.date) {
                            EMPTY_DAY_MESSAGES[
                                java.util.Random(sessionSeed + item.date.toEpochDay()).nextInt(EMPTY_DAY_MESSAGES.size)
                            ]
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No events")
                            Text(message)
                        }
                    }

                    Kind.OWN_EVENT -> item.myEvent?.let {
                        EventCardFor(it, allTasks, categories, viewModel)
                    }

                    Kind.DEVICE_EVENT -> item.deviceEvent?.let { event ->
                        DeviceEventCard(event) { viewModel.importEvent(event) }
                    }

                    Kind.TASK, Kind.ANYTIME_TASK -> item.task?.let { task ->
                        CardBox {
                            TaskRow(
                                task = task,
                                color = (task.color ?: task.categoryId?.let { categoryById[it]?.color })
                                    ?.let { Color(it) },
                                onToggle = { viewModel.toggleDone(task) },
                                onEdit = { viewModel.startEdit(task) },
                                onDelete = { viewModel.delete(task) }
                            )
                        }
                    }
                }
            }
        }

        // Floating "Add on 24 Oct +" pill
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(colors.primary, RoundedCornerShape(4.dp))
                .border(2.dp, colors.outline, RoundedCornerShape(4.dp))
                .clickable {
                    addTarget = addDate
                    showChooser = true
                }
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ADD ON ${addDate.format(DateTimeFormatter.ofPattern("d MMM")).uppercase()}  +",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onPrimary
            )
        }
    }

    if (showChooser) {
        AlertDialog(
            onDismissRequest = { showChooser = false },
            title = {
                Text("Add on ${addTarget.format(DateTimeFormatter.ofPattern("EEE, d MMM"))}")
            },
            confirmButton = {
                Row {
                    TextButton(onClick = { showChooser = false; showNewEvent = true }) { Text("EVENT") }
                    TextButton(onClick = { showChooser = false; showNewTask = true }) { Text("TASK") }
                }
            },
            dismissButton = { TextButton(onClick = { showChooser = false }) { Text("Cancel") } }
        )
    }

    if (showNewEvent) {
        NewEventDialog(
            categories = categories,
            defaultDay = addTarget,
            onDismiss = { showNewEvent = false },
            onCreate = { title, start, end, categoryId ->
                viewModel.addEvent(title, start, end, categoryId)
                showNewEvent = false
            }
        )
    }

    if (showNewTask) {
        NewTaskDialog(
            categories = categories,
            defaultDay = addTarget,
            onDismiss = { showNewTask = false },
            onCreate = { title, categoryId, priority, start, end ->
                viewModel.addTask(title, categoryId, priority, start, end)
                showNewTask = false
            }
        )
    }
}

@Composable
private fun CardBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp)
    ) { content() }
}

@Composable
private fun DeviceEventCard(event: DeviceEvent, onAddAsEvent: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val zone = ZoneId.systemDefault()
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    val label = if (event.allDay) {
        "All day"
    } else {
        val from = Instant.ofEpochMilli(event.startMillis).atZone(zone).format(timeFormat)
        val to = Instant.ofEpochMilli(event.endMillis).atZone(zone).format(timeFormat)
        "$from - $to"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(2.dp, colors.outline, RoundedCornerShape(4.dp))
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(8.dp).fillMaxHeight().background(Color(event.color)))
        Column(modifier = Modifier.weight(1f).padding(10.dp)) {
            Text(text = event.title, fontWeight = FontWeight.Bold)
            Text(
                text = "$label | ${event.calendarName}",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
        }
        TextButton(onClick = onAddAsEvent) { Text("+ Event") }
    }
}