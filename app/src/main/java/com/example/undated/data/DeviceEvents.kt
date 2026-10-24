package com.example.undated.data

import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DeviceEvent(
    val id: Long,
    val title: String,
    val startDay: Long,      // epoch day
    val endDay: Long,        // epoch day, inclusive
    val allDay: Boolean,
    val startMillis: Long,
    val endMillis: Long,
    val color: Int,
    val calendarName: String
)

fun readDeviceEvents(context: Context, from: LocalDate, to: LocalDate): List<DeviceEvent> {
    val zone = ZoneId.systemDefault()
    val rangeStart = from.atStartOfDay(zone).toInstant().toEpochMilli()
    val rangeEnd = to.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

    val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
        ContentUris.appendId(it, rangeStart)
        ContentUris.appendId(it, rangeEnd)
    }.build()

    val projection = arrayOf(
        CalendarContract.Instances.EVENT_ID,                 // 0
        CalendarContract.Instances.TITLE,                    // 1
        CalendarContract.Instances.BEGIN,                    // 2
        CalendarContract.Instances.END,                      // 3
        CalendarContract.Instances.ALL_DAY,                  // 4
        CalendarContract.Instances.CALENDAR_COLOR,           // 5
        CalendarContract.Instances.CALENDAR_DISPLAY_NAME     // 6
    )

    val result = mutableListOf<DeviceEvent>()
    try {
        context.contentResolver.query(
            uri,
            projection,
            "${CalendarContract.Instances.VISIBLE} = 1",
            null,
            "${CalendarContract.Instances.BEGIN} ASC"
        )?.use { c ->
            while (c.moveToNext()) {
                val begin = c.getLong(2)
                val end = c.getLong(3)
                val allDay = c.getInt(4) == 1

                val startDay: Long
                val endDay: Long
                if (allDay) {
                    // All-day events are stored as UTC midnights, and the end is exclusive.
                    startDay = begin / 86_400_000L
                    endDay = maxOf(startDay, end / 86_400_000L - 1)
                } else {
                    startDay = Instant.ofEpochMilli(begin).atZone(zone).toLocalDate().toEpochDay()
                    endDay = Instant.ofEpochMilli(maxOf(begin, end - 1))
                        .atZone(zone).toLocalDate().toEpochDay()
                }

                result += DeviceEvent(
                    id = c.getLong(0),
                    title = c.getString(1)?.takeIf { it.isNotBlank() } ?: "(no title)",
                    startDay = startDay,
                    endDay = endDay,
                    allDay = allDay,
                    startMillis = begin,
                    endMillis = end,
                    color = c.getInt(5),
                    calendarName = c.getString(6) ?: ""
                )
            }
        }
    } catch (e: SecurityException) {
        // Permission not granted yet, so there's nothing to show.
    }
    return result
}