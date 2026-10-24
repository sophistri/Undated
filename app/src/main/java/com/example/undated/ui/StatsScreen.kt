package com.example.undated.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate

private data class CategoryStat(
    val name: String,
    val color: Color,
    val total: Int,
    val done: Int
)

@Composable
fun StatsScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val today = remember { LocalDate.now().toEpochDay() }

    if (tasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PixelSunflower(modifier = Modifier.size(64.dp), spinning = false)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Add some tasks to see stats",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        return
    }

    val total = tasks.size
    val done = tasks.count { it.isDone }
    val open = tasks.filter { !it.isDone }
    val percent = done * 100 / total

    // Sort every open task into exactly one timing bucket.
    var overdue = 0
    var thisWeek = 0
    var later = 0
    var noDate = 0
    open.forEach { t ->
        val start = t.dueStart
        val end = t.dueEnd ?: start
        when {
            start == null || end == null -> noDate++
            end < today -> overdue++
            start <= today + 6 -> thisWeek++
            else -> later++
        }
    }

    val byCategory = tasks.groupBy { it.categoryId }
    val categoryRows = categories.mapNotNull { c ->
        val list = byCategory[c.id] ?: return@mapNotNull null
        CategoryStat(c.name, Color(c.color), list.size, list.count { it.isDone })
    } + (byCategory[null]?.let { list ->
        listOf(CategoryStat("none", colors.outline, list.size, list.count { it.isDone }))
    } ?: emptyList())
    val maxCategory = categoryRows.maxOfOrNull { it.total } ?: 0

    val dim = colors.outline.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(total.toString(), "TOTAL", Modifier.weight(1f))
            StatTile(open.size.toString(), "OPEN", Modifier.weight(1f))
            StatTile(done.toString(), "DONE", Modifier.weight(1f))
            StatTile("$percent%", "RATE", Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        BarRow("progress", done, total, colors.primary, "$percent%")

        SectionTitle("OPEN BY TIMING")
        BarRow("overdue", overdue, open.size, colors.primary, "$overdue")
        BarRow("this week", thisWeek, open.size, colors.tertiary, "$thisWeek")
        BarRow("later", later, open.size, colors.secondary, "$later")
        BarRow("no date", noDate, open.size, dim, "$noDate")

        SectionTitle("BY CATEGORY")
        categoryRows.forEach { row ->
            BarRow(row.name, row.total, maxCategory, row.color, "${row.done}/${row.total}")
        }

        SectionTitle("OPEN BY PRIORITY")
        val openCount = open.size
        BarRow("high", open.count { it.priority == 3 }, openCount, colors.primary,
            "${open.count { it.priority == 3 }}")
        BarRow("medium", open.count { it.priority == 2 }, openCount, colors.tertiary,
            "${open.count { it.priority == 2 }}")
        BarRow("low", open.count { it.priority == 1 }, openCount, colors.secondary,
            "${open.count { it.priority == 1 }}")
        BarRow("none", open.count { it.priority == 0 }, openCount, dim,
            "${open.count { it.priority == 0 }}")
    }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(16.dp))
    Text(text = "== $text ==", style = MaterialTheme.typography.labelLarge)
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 6.dp),
        thickness = 2.dp,
        color = MaterialTheme.colorScheme.outline
    )
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(4.dp)
    Column(
        modifier = modifier
            .background(colors.surfaceVariant, shape)
            .border(2.dp, colors.outline, shape)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = colors.primary
        )
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun BarRow(label: String, value: Int, max: Int, color: Color, trailing: String) {
    val outline = MaterialTheme.colorScheme.outline
    val fraction = if (max == 0) 0f else value.toFloat() / max

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(90.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .border(2.dp, outline, RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(color)
            )
        }
        Text(
            text = trailing,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.End,
            modifier = Modifier.width(52.dp)
        )
    }
}