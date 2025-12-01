package com.example.calculator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import kotlin.math.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.calculator.ui.utils.VSpacer
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** Dialog container for the color picker */
@Composable
fun CircularColorPicker(onDismissRequest: () -> Unit, onConfirm: (Color) -> Unit) {
    var selectedColor by remember { mutableStateOf(Color.White) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Pick a Color") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularColorCanvas(
                    modifier = Modifier.size(250.dp),
                    onColorSelected = { color -> selectedColor = color }
                )
                VSpacer(12)
                Row {
                    Text(
                        text = "Selected Color: "
                    )
                    Text(
                        text = "#${selectedColor.value.toULong().toString(16)}",
                        color = selectedColor
                    )
                }

            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text("CANCEL")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedColor)
            }) {
                Text("CONFIRM")
            }
        }
    )
}

/** A circular canvas filled with hues the user can tap on to pick a color */
@Composable
fun CircularColorCanvas(
    modifier: Modifier = Modifier,
    onColorSelected: (Color) -> Unit = {}
) {
    var selectorOffset by remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                // Unified gesture handler
                detectDragGestures(
                    onDragStart = { offset -> selectorOffset = offset },
                    onDrag = { change, _ -> selectorOffset = change.position },
                    onDragEnd = { /* nothing */ }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    selectorOffset = offset
                }
            }
    ) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Sweep gradient (hue)
        val hueBrush = Brush.sweepGradient(
            colors = listOf(
                Color.Red, Color.Yellow, Color.Green,
                Color.Cyan, Color.Blue, Color.Magenta, Color.Red
            ),
            center = center
        )

        // Radial gradient (saturation)
        val saturationBrush = Brush.radialGradient(
            colors = listOf(Color.White, Color.Transparent),
            center = center,
            radius = radius
        )

        // Draw color wheel
        drawCircle(brush = hueBrush, radius = radius, center = center)
        drawCircle(brush = saturationBrush, radius = radius, center = center)

        // Draw selector if any
        selectorOffset?.let { offset ->
            val dx = offset.x - center.x
            val dy = offset.y - center.y
            val distance = hypot(dx, dy)
            val clampedDistance = distance.coerceIn(0f, radius)
            val angle = (atan2(dy, dx).toDouble().toDegrees() + 360) % 360

            val hue = angle.toFloat()
            val sat = (clampedDistance / radius).coerceIn(0f, 1f)
            val selectedColor = Color.hsv(hue, sat, 1f)

            // notify parent
            onColorSelected(selectedColor)

            // selector position (clamped)
            val selectorX = center.x + cos(angle.toRadians()) * clampedDistance
            val selectorY = center.y + sin(angle.toRadians()) * clampedDistance

            // white border circle
            drawCircle(
                color = Color.White,
                radius = 38f,
                center = Offset(selectorX.toFloat(), selectorY.toFloat())
            )
            // inner color circle
            drawCircle(
                color = selectedColor,
                radius = 30f,
                center = Offset(selectorX.toFloat(), selectorY.toFloat())
            )
        }
    }
}

/**
 * Converts an angle measured in radians to an approximately equivalent angle measured in degrees.
 * The conversion from radians to degrees is generally inexact.
 */
private fun Double.toDegrees(): Double = this * 180.0 / kotlin.math.PI

/**
 * Converts an angle measured in degrees to an approximately equivalent angle measured in radians.
 * The conversion from degrees to radians is generally inexact.
 */
private fun Double.toRadians(): Double = this * kotlin.math.PI / 180.0
