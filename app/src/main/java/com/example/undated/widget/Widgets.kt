package com.example.undated.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

suspend fun refreshWidgets(context: Context) {
    TaskWidget().updateAll(context)
    CalendarWidget().updateAll(context)
}