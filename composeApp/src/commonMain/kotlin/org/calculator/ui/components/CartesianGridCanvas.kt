package org.calculator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Draws a pannable/zoomable Cartesian grid.
 *
 * Caller supplies:
 *   [onPan]         – called with pixel delta (dx, dy)
 *   [onZoom]        – called with (scaleFactor, focalCanvasX, focalCanvasY)
 *   [onResetView]   – reset to default viewport
 *   [drawExtra]     – additional drawing on top of the grid
 *   [onCursorMoved] – reports (canvasX, canvasY, originX, originY)
 */
@Composable
fun CartesianGridCanvas(
    scale:        Float,
    offsetX:      Float,
    offsetY:      Float,
    step:         Float,
    onPan:        (Float, Float) -> Unit,
    onZoom:       (factor: Float, focalX: Float, focalY: Float) -> Unit,
    onResetView:  () -> Unit,
    drawExtra:    (DrawScope, Float, Float) -> Unit,
    onCursorMoved:(canvasX: Float, canvasY: Float, originX: Float, originY: Float) -> Unit = { _, _, _, _ -> }
) {
    val textMeasurer = rememberTextMeasurer()
    val baseTextStyle = TextStyle(
        color     = Color.DarkGray,
        fontSize  = 12.sp,
        textAlign = TextAlign.Center
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Track cursor position
            .pointerInput(offsetX, offsetY, scale) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pos = event.changes.firstOrNull()?.position ?: continue
                        val originX = size.width  / 2f + offsetX
                        val originY = size.height / 2f + offsetY
                        onCursorMoved(pos.x, pos.y, originX, originY)
                    }
                }
            }
            // Pinch-to-zoom + two-finger pan
            .pointerInput(offsetX, offsetY, scale) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    if (zoom != 1f) {
                        // centroid is relative to the composable, not the graph origin
                        onZoom(zoom, centroid.x - size.width / 2f, centroid.y - size.height / 2f)
                    }
                    if (pan != Offset.Zero) onPan(pan.x, pan.y)
                }
            }
            // Single-finger drag (pan)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onPan(dragAmount.x, dragAmount.y)
                }
            }
            // Double-tap → reset
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onResetView() })
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val originX = size.width  / 2f + offsetX
            val originY = size.height / 2f + offsetY

            // Visible graph-coordinate range
            val minX = -originX / (step * scale)
            val maxX = (size.width  - originX) / (step * scale)
            val minY = (originY - size.height) / (step * scale)
            val maxY =  originY / (step * scale)

            // Adaptive grid step (in graph units)
            val gridStep: Float = when {
                scale > 20f -> 0.1f
                scale > 10f -> 0.2f
                scale > 5f  -> 0.5f
                scale > 2f  -> 1f
                scale > 0.5f-> 2f
                scale > 0.2f-> 5f
                else        -> 10f
            }

            // --- Vertical grid lines ---
            val startX  = (minX / gridStep).toInt() * gridStep - gridStep
            val endX    = maxX + gridStep * 2
            var gx = startX
            while (gx <= endX) {
                val xPos = originX + gx * step * scale
                if (xPos in -100f..(size.width + 100)) {
                    val isInteger = gx.toInt().toFloat() == gx
                    drawLine(
                        color       = Color.DarkGray.copy(alpha = if (isInteger) 0.4f else 0.15f),
                        start       = Offset(xPos, 0f),
                        end         = Offset(xPos, size.height),
                        strokeWidth = if (isInteger) 1f else 0.5f
                    )
                }
                gx += gridStep
            }

            // --- Horizontal grid lines ---
            val startY = (minY / gridStep).toInt() * gridStep - gridStep
            val endY   = maxY + gridStep * 2
            var gy = startY
            while (gy <= endY) {
                val yPos = originY - gy * step * scale
                if (yPos in -100f..(size.height + 100)) {
                    val isInteger = gy.toInt().toFloat() == gy
                    drawLine(
                        color       = Color.DarkGray.copy(alpha = if (isInteger) 0.4f else 0.15f),
                        start       = Offset(0f, yPos),
                        end         = Offset(size.width, yPos),
                        strokeWidth = if (isInteger) 1f else 0.5f
                    )
                }
                gy += gridStep
            }

            // --- Axes (drawn ONCE) ---
            val axisWidth = 2f * scale.coerceIn(0.5f, 2f)
            drawLine(Color.Black, Offset(0f, originY), Offset(size.width, originY), axisWidth)
            drawLine(Color.Black, Offset(originX, 0f), Offset(originX, size.height), axisWidth)

            // --- Axis labels ---
            val scaledStyle = baseTextStyle.copy(fontSize = (12f * scale.coerceIn(0.5f, 2f)).sp)

            // X-axis labels
            gx = startX
            while (gx <= endX) {
                if (gx.toInt().toFloat() == gx && gx != 0f) {
                    val xPos = originX + gx * step * scale
                    if (xPos in 0f..size.width) {
                        val text = gx.toInt().toString()
                        val measured = textMeasurer.measure(text, scaledStyle, maxLines = 1)
                        val ty = (originY + 6f * scale.coerceIn(0.5f, 2f))
                            .coerceIn(0f, size.height - measured.size.height)
                        drawText(
                            textMeasurer, text,
                            topLeft = Offset(xPos - measured.size.width / 2f, ty),
                            style = scaledStyle
                        )
                    }
                }
                gx += gridStep
            }

            // Y-axis labels
            gy = startY
            while (gy <= endY) {
                if (gy.toInt().toFloat() == gy && gy != 0f) {
                    val yPos = originY - gy * step * scale
                    if (yPos in 0f..size.height) {
                        val text = gy.toInt().toString()
                        val measured = textMeasurer.measure(text, scaledStyle, maxLines = 1)
                        val tx = (originX - measured.size.width - 6f * scale.coerceIn(0.5f, 2f))
                            .coerceAtLeast(0f)
                        drawText(
                            textMeasurer, text,
                            topLeft = Offset(tx, yPos - measured.size.height / 2f),
                            style = scaledStyle
                        )
                    }
                }
                gy += gridStep
            }

            // Origin label — only show when origin is close to centre
            if (originX in 20f..(size.width - 20f) && originY in 20f..(size.height - 20f)) {
                drawText(
                    textMeasurer, "O",
                    topLeft = Offset(originX + 4f, originY + 4f),
                    style = scaledStyle
                )
            }

            // --- Custom content (functions, plots, …) ---
            drawExtra(this, originX, originY)
        }

        // --- On-canvas zoom / reset buttons ---
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                .padding(4.dp)
        ) {
            IconButton(onClick = { onZoom(2f, 0f, 0f) }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.ZoomIn,  contentDescription = "Zoom In")
            }
            IconButton(onClick = { onZoom(0.5f, 0f, 0f) }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
            }
            IconButton(
                onClick  = { onResetView() },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Filled.RestartAlt, contentDescription = "Reset View", modifier = Modifier.size(20.dp))
            }
        }
    }
}
