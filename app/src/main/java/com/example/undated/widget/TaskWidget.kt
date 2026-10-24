package com.example.undated.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.undated.MainActivity
import com.example.undated.data.AppDatabase
import com.example.undated.data.Task

import android.content.Intent
import androidx.glance.LocalContext
import com.example.undated.ui.theme.UndatedWidgetColors

class TaskWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tasks = AppDatabase.getInstance(context).taskDao().getOpen()
        provideContent {
            GlanceTheme(colors = UndatedWidgetColors) {
                WidgetContent(tasks)
            }
        }
    }
}

@Composable
private fun WidgetContent(tasks: List<Task>) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(12.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        Text(
            text = "Undated",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface
            )
        )
        Spacer(GlanceModifier.height(8.dp))

        if (tasks.isEmpty()) {
            Text(
                text = "Nothing pending",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
            )
        } else {
            LazyColumn {
                items(tasks, itemId = { it.id }) { task ->
                    Text(
                        text = task.title,
                        modifier = GlanceModifier.padding(vertical = 4.dp),
                        style = TextStyle(color = GlanceTheme.colors.onSurface)
                    )
                }
            }
        }
    }
}