package org.calculator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.calculator.ui.utils.VSpacer
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** Dialog container for the color picker */
@Composable
fun CircularColorPicker(onDismissRequest: () -> Unit, onConfirm: (Color) -> Unit) {
    var selectedColorCircular by remember { mutableStateOf(Color.White) }
    var finalColor by remember { mutableStateOf(Color.White) }

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
                    onColorSelected = { color ->
                        selectedColorCircular = color
                        finalColor = color
                    }
                )
                VSpacer(12)
                Row {
                    Text(
                        text = "Selected Color: "
                    )
                    Text(
                        text = "#${finalColor.value.toULong().toString(16)}",
                        color = finalColor
                    )
                }
                VSpacer(12)
                GrayscaleBar(
                    modifier = Modifier.size(height = 80.dp, width = 300.dp),
                    currentColor = selectedColorCircular,
                    onColorSelected = { color -> finalColor = color }
                )
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
                onConfirm(finalColor)
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

@Composable
fun GrayscaleBar(
    modifier: Modifier = Modifier,
    currentColor: Color = Color.White,
    onColorSelected: (Color) -> Unit = {}
){
    // Remember the original hue color (before grayscale adjustment)
    val originalColor = remember(currentColor) {
        // Extract the pure hue by setting saturation and value to max
        val r = currentColor.red
        val g = currentColor.green
        val b = currentColor.blue

        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min

        val hue = when {
            delta == 0f -> 0f
            max == r -> 60f * (((g - b) / delta) % 6f)
            max == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }.let { if (it < 0) it + 360f else it }

        val saturation = if (max == 0f) 0f else delta / max

        // Store the color with full value for reference
        Color.hsv(hue, saturation, 1f)
    }

    var selectorOffset by remember { mutableStateOf<Offset?>(null) }

    remember(currentColor) {
        selectorOffset = null
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
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
    ){
        val width = size.width
        val height = size.height
        val cornerRadius = height / 4

        // Gradient: White -> OriginalColor -> Black
        val grayscaleBrush = Brush.horizontalGradient(
            colors = listOf(
                Color.White,
                originalColor,
                Color.Black
            ),
            startX = 0f,
            endX = width
        )

        drawRoundRect(
            brush = grayscaleBrush,
            cornerRadius = CornerRadius(cornerRadius)
        )

        selectorOffset?.let { offset ->
            // Clamp X position to bar width
            val clampedX = offset.x.coerceIn(0f, width)
            val selectorY = height / 2

            // Calculate position ratio (0.0 to 1.0)
            val ratio = clampedX / width

            // Extract HSV from original color
            val r = originalColor.red
            val g = originalColor.green
            val b = originalColor.blue

            val max = maxOf(r, g, b)
            val min = minOf(r, g, b)
            val delta = max - min

            val hue = when {
                delta == 0f -> 0f
                max == r -> 60f * (((g - b) / delta) % 6f)
                max == g -> 60f * (((b - r) / delta) + 2f)
                else -> 60f * (((r - g) / delta) + 4f)
            }.let { if (it < 0) it + 360f else it }

            val saturation = if (max == 0f) 0f else delta / max

            // Adjust only the value (brightness) based on position
            val value = when {
                ratio < 0.5f -> {
                    // White -> CurrentColor (first half)
                    val t = ratio * 2 // 0.0 to 1.0
                    lerp(1f, 1f, t) // Full brightness on left half
                }
                else -> {
                    // CurrentColor -> Black (second half)
                    val t = (ratio - 0.5f) * 2 // 0.0 to 1.0
                    lerp(1f, 0f, t) // Reduce brightness on right half
                }
            }

            // Adjust saturation for the white side
            val adjustedSaturation = when {
                ratio < 0.5f -> {
                    val t = ratio * 2
                    lerp(0f, saturation, t) // Desaturate towards white
                }
                else -> saturation // Keep full saturation on right side
            }

            // Create color with preserved hue, adjusted saturation and value
            val selectedColor = Color.hsv(hue, adjustedSaturation.coerceIn(0f, 1f), value.coerceIn(0f, 1f))

            // Notify parent
            onColorSelected(selectedColor)

            // Draw selector (white border + color fill)
            drawCircle(
                color = Color.White,
                radius = height / 2 + 4f,
                center = Offset(clampedX, selectorY)
            )
            drawCircle(
                color = selectedColor,
                radius = height / 2 - 2f,
                center = Offset(clampedX, selectorY)
            )
        }
    }
}

// Helper function for linear interpolation
private fun lerp(start: Float, end: Float, t: Float): Float {
    return start + (end - start) * t
}

/**
 * Converts an angle measured in radians to an approximately equivalent angle measured in degrees.
 * The conversion from radians to degrees is generally inexact.
 */
private fun Double.toDegrees(): Double = this * 180.0 / PI

/**
 * Converts an angle measured in degrees to an approximately equivalent angle measured in radians.
 * The conversion from degrees to radians is generally inexact.
 */
private fun Double.toRadians(): Double = this * PI / 180.0
