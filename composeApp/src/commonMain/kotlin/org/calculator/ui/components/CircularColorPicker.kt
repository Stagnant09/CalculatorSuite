package org.calculator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.calculator.ui.utils.VSpacer
import kotlin.math.*

// ---------------------------------------------------------------------------
// Dialog wrapper
// ---------------------------------------------------------------------------

@Composable
fun CircularColorPicker(onDismissRequest: () -> Unit, onConfirm: (Color) -> Unit) {
    var selectedColorCircular by remember { mutableStateOf(Color.hsl(0f, 1f, 0.5f)) }
    var finalColor            by remember { mutableStateOf(Color.hsl(0f, 1f, 0.5f)) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title   = { Text("Pick a Color") },
        text    = {
            Column(
                modifier              = Modifier.fillMaxWidth(),
                horizontalAlignment   = Alignment.CenterHorizontally
            ) {
                CircularColorCanvas(
                    modifier          = Modifier.size(250.dp),
                    onColorSelected   = { color ->
                        selectedColorCircular = color
                        finalColor = color
                    }
                )
                VSpacer(12)
                // Preview swatch + hex label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(24.dp)) {
                        drawCircle(finalColor)
                        drawCircle(Color.Black.copy(alpha = 0.25f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = "#${finalColor.toHex()}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                VSpacer(12)
                GrayscaleBar(
                    modifier          = Modifier.size(height = 48.dp, width = 300.dp),
                    currentColor      = selectedColorCircular,
                    onColorSelected   = { color -> finalColor = color }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("CANCEL") }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(finalColor) }) { Text("CONFIRM") }
        }
    )
}

// ---------------------------------------------------------------------------
// Hue / saturation wheel
// ---------------------------------------------------------------------------

@Composable
fun CircularColorCanvas(
    modifier: Modifier = Modifier,
    onColorSelected: (Color) -> Unit = {}
) {
    var selectorOffset by remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { selectorOffset = it },
                    onDrag      = { change, _ -> selectorOffset = change.position }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { selectorOffset = it }
            }
    ) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Hue sweep
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red),
                center = center
            ),
            radius = radius,
            center = center
        )
        // Saturation (white radial overlay)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color.Transparent),
                center = center, radius = radius
            ),
            radius = radius, center = center
        )

        selectorOffset?.let { offset ->
            val dx       = offset.x - center.x
            val dy       = offset.y - center.y
            val dist     = hypot(dx, dy)
            val clamped  = dist.coerceIn(0f, radius)
            val angleDeg = ((atan2(dy, dx) * 180.0 / PI) + 360) % 360
            val hue      = angleDeg.toFloat()
            val sat      = (clamped / radius).coerceIn(0f, 1f)
            val color    = Color.hsv(hue, sat, 1f)
            onColorSelected(color)

            val sx = (center.x + cos(angleDeg * PI / 180) * clamped).toFloat()
            val sy = (center.y + sin(angleDeg * PI / 180) * clamped).toFloat()
            drawCircle(Color.White, 16f, Offset(sx, sy))
            drawCircle(color, 12f, Offset(sx, sy))
        }
    }
}

// ---------------------------------------------------------------------------
// Brightness / saturation bar  (White ← Hue → Black)
//
// Bug fixed: the original code had  lerp(1f, 1f, t)  in the white half,
// which always returned 1 (full brightness) regardless of t.  The correct
// behaviour is to keep value=1 and interpolate saturation from 0→full in the
// left half, then keep saturation=full and interpolate value from 1→0 in the
// right half.
// ---------------------------------------------------------------------------

@Composable
fun GrayscaleBar(
    modifier: Modifier = Modifier,
    currentColor: Color = Color.Red,
    onColorSelected: (Color) -> Unit = {}
) {
    // Extract stable hue + saturation from whatever colour arrived from the wheel
    data class HueSat(val hue: Float, val sat: Float)

    val hueSat = remember(currentColor) {
        val r = currentColor.red; val g = currentColor.green; val b = currentColor.blue
        val max = maxOf(r, g, b); val min = minOf(r, g, b); val delta = max - min
        val hue = when {
            delta == 0f -> 0f
            max == r    -> 60f * (((g - b) / delta) % 6f)
            max == g    -> 60f * (((b - r) / delta) + 2f)
            else        -> 60f * (((r - g) / delta) + 4f)
        }.let { if (it < 0) it + 360f else it }
        HueSat(hue, if (max == 0f) 0f else delta / max)
    }

    val pureHueColor = Color.hsv(hueSat.hue, hueSat.sat, 1f)

    var selectorOffset by remember { mutableStateOf<Offset?>(null) }
    // Reset selector when the hue/sat changes
    LaunchedEffect(hueSat) { selectorOffset = null }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { selectorOffset = it },
                    onDrag      = { change, _ -> selectorOffset = change.position }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { selectorOffset = it }
            }
    ) {
        val w = size.width; val h = size.height
        val cr = CornerRadius(h / 4)

        // Gradient: White → PureHue → Black
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.White, pureHueColor, Color.Black),
                startX = 0f, endX = w
            ),
            cornerRadius = cr
        )

        selectorOffset?.let { offset ->
            val cx    = offset.x.coerceIn(0f, w)
            val ratio = cx / w          // 0 = white, 0.5 = pure hue, 1 = black

            // LEFT half  (0 → 0.5): value = 1, saturation goes 0 → full
            // RIGHT half (0.5 → 1): saturation = full, value goes 1 → 0
            val (adjSat, adjVal) = when {
                ratio < 0.5f -> {
                    val t = ratio * 2f                          // 0 → 1 across left half
                    lerp(0f, hueSat.sat, t) to 1f              // desaturate → pure hue
                }
                else -> {
                    val t = (ratio - 0.5f) * 2f                // 0 → 1 across right half
                    hueSat.sat to lerp(1f, 0f, t)              // darken → black
                }
            }

            val selectedColor = Color.hsv(
                hueSat.hue,
                adjSat.coerceIn(0f, 1f),
                adjVal.coerceIn(0f, 1f)
            )
            onColorSelected(selectedColor)

            val cy = h / 2
            drawCircle(Color.White, h / 2 + 4f, Offset(cx, cy))
            drawCircle(selectedColor, h / 2 - 2f, Offset(cx, cy))
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

fun Color.toHex(): String {
    fun Int.hex2() = toString(16).padStart(2, '0').uppercase()
    return "${(red   * 255).toInt().hex2()}" +
           "${(green * 255).toInt().hex2()}" +
           "${(blue  * 255).toInt().hex2()}"
}
