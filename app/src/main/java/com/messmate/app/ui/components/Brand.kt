package com.messmate.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.messmate.app.ui.theme.MessMatePalette

/** MessMate's plate-clock emblem pairs the dining tools with simple meal-time hands. */
@Composable
fun MessMateLogo(modifier: Modifier = Modifier, size: Dp = 42.dp) {
    // Resolve composition-local theme colors before entering Canvas's non-composable draw scope.
    val green = MessMatePalette.Forest
    val orange = MessMatePalette.Saffron
    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "MessMate plate and clock logo" },
    ) {
        val w = this.size.width
        val h = this.size.height
        val unit = minOf(w, h)
        val stroke = unit * 0.055f
        val plateCenter = Offset(w * 0.50f, h * 0.54f)
        val plateRadius = unit * 0.30f

        // Fork silhouette at left and spoon silhouette at right keep the food cue clear at small sizes.
        val forkX = w * 0.16f
        for (offset in listOf(-0.045f, 0f, 0.045f)) {
            drawLine(green, Offset(forkX + unit * offset, h * 0.18f), Offset(forkX + unit * offset, h * 0.36f), strokeWidth = stroke)
        }
        drawLine(green, Offset(forkX - unit * 0.055f, h * 0.36f), Offset(forkX + unit * 0.055f, h * 0.36f), strokeWidth = stroke)
        drawLine(green, Offset(forkX, h * 0.36f), Offset(forkX, h * 0.83f), strokeWidth = stroke)

        val spoonX = w * 0.84f
        drawOval(
            color = green,
            topLeft = Offset(spoonX - unit * 0.07f, h * 0.16f),
            size = Size(unit * 0.14f, h * 0.22f),
        )
        drawLine(green, Offset(spoonX, h * 0.36f), Offset(spoonX, h * 0.83f), strokeWidth = stroke)

        drawCircle(green, radius = plateRadius, center = plateCenter, style = Stroke(width = stroke))
        drawCircle(green.copy(alpha = 0.20f), radius = plateRadius * 0.79f, center = plateCenter, style = Stroke(width = stroke * 0.65f))
        drawLine(
            orange,
            Offset(plateCenter.x, plateCenter.y - plateRadius * 0.48f),
            Offset(plateCenter.x, plateCenter.y + plateRadius * 0.08f),
            strokeWidth = stroke * 1.2f,
        )
        drawLine(
            orange,
            Offset(plateCenter.x, plateCenter.y + plateRadius * 0.08f),
            Offset(plateCenter.x + plateRadius * 0.42f, plateCenter.y + plateRadius * 0.34f),
            strokeWidth = stroke * 1.2f,
        )
        drawCircle(orange, radius = stroke * 1.35f, center = plateCenter)
    }
}

@Composable
fun MessMateWordmark(modifier: Modifier = Modifier, markSize: Dp = 40.dp, dark: Boolean = false) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        MessMateLogo(size = markSize)
        Spacer(Modifier.width(10.dp))
        Text(
            text = "MessMate",
            color = if (dark) MessMatePalette.White else MessMatePalette.Forest,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.55).sp,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
fun Eyebrow(text: String, color: Color = MessMatePalette.SageDark, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
    )
}
