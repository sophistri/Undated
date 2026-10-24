package com.example.undated.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun RetroWindow(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(4.dp)

    Column(
        modifier = modifier
            .border(2.dp, colors.outline, shape)
            .clip(shape)
            .background(colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primary)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = colors.onPrimary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "⋆\uD801\uDE5A₊˚⊹♡",
                color = colors.onPrimary,
                style = MaterialTheme.typography.labelLarge
            )
        }
        HorizontalDivider(thickness = 2.dp, color = colors.outline)
        content()
    }
}