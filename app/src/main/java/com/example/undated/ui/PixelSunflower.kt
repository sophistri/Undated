package com.example.undated.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

// 17x17 pixel sunflower. Each letter is one pixel; '.' is transparent.
private val SUNFLOWER = listOf(
    ".......OOO.......",
    ".......OYO.......",
    "...OO.OYYYO.OO...",
    "..OYYOOYLYOOYYO..",
    "..OYLYYYYYYYLYO..",
    "...OYLYDBDYLYO...",
    "..OOYYDBDBDYYYO..",
    "OOYYYDBDBDBDYYYOO",
    "OYYLYBDBGBDBYLYYO",
    "OOYYYDBDBDBDYYYOO",
    "..OOYYDBDBDYYOO..",
    "...OYLYDBDYLYO...",
    "..OYLYYYYYYYLYO..",
    "..OYYOYYLYOOYYO..",
    "...OO.OYYYO.OO...",
    ".......OYO.......",
    ".......OOO......."
)

private val SUNFLOWER_COLORS = mapOf(
    'O' to Color(0xFFD97A0B), // outline
    'Y' to Color(0xFFFFD23F), // petal
    'L' to Color(0xFFFFEE9C), // petal highlight
    'B' to Color(0xFF7A4A12), // seed brown
    'D' to Color(0xFF4A2A0A), // seed dark
    'G' to Color(0xFFE8A82C)  // centre
)

@Composable
fun PixelSunflower(
    modifier: Modifier = Modifier,
    spinning: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "sunflower")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val cell = size.width / SUNFLOWER.size
        rotate(degrees = if (spinning) angle else 0f) {
            SUNFLOWER.forEachIndexed { y, row ->
                row.forEachIndexed { x, ch ->
                    SUNFLOWER_COLORS[ch]?.let { color ->
                        drawRect(
                            color = color,
                            topLeft = Offset(x * cell, y * cell),
                            size = Size(cell + 0.5f, cell + 0.5f) // tiny overlap hides seams
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SunflowerSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PixelSunflower(modifier = Modifier.size(128.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                text = "loading...",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}