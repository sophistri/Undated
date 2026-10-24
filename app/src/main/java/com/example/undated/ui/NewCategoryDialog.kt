package com.example.undated.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val CATEGORY_COLORS = listOf(
    0xFFAE4948, 0xFFD48354, 0xFFE0B84C, 0xFF5E8C6A,
    0xFF4F8FA3, 0xFF7A5C9E, 0xFFD9729B, 0xFF8A6E4B
).map { it.toInt() }

@Composable
fun NewCategoryDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, color: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableIntStateOf(CATEGORY_COLORS.first()) }
    val shape = RoundedCornerShape(4.dp)
    val ink = MaterialTheme.colorScheme.outline

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New category") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Name") },
                    singleLine = true,
                    shape = shape
                )
                Spacer(Modifier.height(16.dp))
                CATEGORY_COLORS.chunked(4).forEach { rowColors ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        rowColors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(c), shape)
                                    .border(if (c == color) 3.dp else 1.dp, ink, shape)
                                    .clickable { color = c }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onCreate(name, color) }
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}