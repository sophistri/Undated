package com.example.undated.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.example.undated.MainActivity
import com.example.undated.data.AppDatabase
import com.example.undated.data.readDeviceEvents
import com.example.undated.ui.theme.UndatedWidgetColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = LocalDate.now()
        val month = YearMonth.from(today)
        val monthStart = month.atDay(1).toEpochDay()
        val monthEnd = month.atEndOfMonth().toEpochDay()

        val (taskDays, eventDays) = withContext(Dispatchers.IO) {
            val tasks = AppDatabase.getInstance(context).taskDao().getOpen()
            val events = readDeviceEvents(context, month.atDay(1), month.atEndOfMonth())

            val taskSet = HashSet<Long>()
            for (t in tasks) {
                val start = t.dueStart ?: continue
                val end = t.dueEnd ?: start
                for (d in maxOf(start, monthStart)..minOf(end, monthEnd)) taskSet += d
            }

            val eventSet = HashSet<Long>()
            for (e in events) {
                for (d in maxOf(e.startDay, monthStart)..minOf(e.endDay, monthEnd)) eventSet += d
            }
            val ownEvents = AppDatabase.getInstance(context).eventDao().getAllOnce()
            for (e in ownEvents) {
                for (d in maxOf(e.startDay, monthStart)..minOf(e.endDay, monthEnd)) eventSet += d
            }

            taskSet to eventSet
        }

        provideContent {
            GlanceTheme(colors = UndatedWidgetColors) {
                CalendarContent(today, month, taskDays, eventDays)
            }
        }
    }
}

private val WEEKDAYS = listOf("S", "M", "T", "W", "T", "F", "S")

@Composable
private fun CalendarContent(
    today: LocalDate,
    month: YearMonth,
    taskDays: Set<Long>,
    eventDays: Set<Long>
) {
    val context = LocalContext.current

    val offset = month.atDay(1).dayOfWeek.value % 7
    val cells: List<LocalDate?> =
        List(offset) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    val weeks = (cells + List((7 - cells.size % 7) % 7) { null }).chunked(7)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(8.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        Text(
            text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")).uppercase(),
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )
        )

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            WEEKDAYS.forEach { d ->
                Text(
                    text = d,
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }

        weeks.forEach { week ->
            Row(modifier = GlanceModifier.fillMaxWidth().height(30.dp)) {
                week.forEach { date ->
                    DayBox(
                        date = date,
                        today = today,
                        hasTask = date != null && date.toEpochDay() in taskDays,
                        hasEvent = date != null && date.toEpochDay() in eventDays
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayBox(
    date: LocalDate?,
    today: LocalDate,
    hasTask: Boolean,
    hasEvent: Boolean
) {
    Box(
        modifier = GlanceModifier.defaultWeight().fillMaxHeight().padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        if (date == null) return@Box

        val isToday = date == today
        var cell = GlanceModifier.fillMaxSize()
        if (isToday) {
            cell = cell.background(GlanceTheme.colors.primary).cornerRadius(4.dp)
        } else if (hasTask) {
            cell = cell.background(GlanceTheme.colors.primaryContainer).cornerRadius(4.dp)
        }

        val textColor = when {
            isToday -> GlanceTheme.colors.onPrimary
            hasTask -> GlanceTheme.colors.onPrimaryContainer
            else -> GlanceTheme.colors.onSurface
        }

        Box(modifier = cell, contentAlignment = Alignment.Center) {
            Text(
                text = date.dayOfMonth.toString(),
                style = TextStyle(
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    textDecoration = if (hasEvent) TextDecoration.Underline else TextDecoration.None
                )
            )
        }
    }
}