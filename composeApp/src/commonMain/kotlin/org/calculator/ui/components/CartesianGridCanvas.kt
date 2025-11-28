package org.calculator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** This composable is a canvas that draws a Cartesian grid
 * with labels and zoom-in/zoom-out buttons
 */
@Composable
fun CartesianGridCanvas(
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    step: Float,
    onPan: (Float, Float) -> Unit,
    onZoomIn: (Float) -> Unit,
    onZoomOut: (Float) -> Unit,
    onResetView: () -> Unit,
    onTap: (Float, Float, Float, Float) -> Unit,
    onDragStart: (Float, Float, Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Float, Float, Float, Float) -> Unit,
    drawExtra: (DrawScope, Float, Float) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val baseTextStyle = TextStyle(
        color = Color.DarkGray,
        fontSize = 16.sp, // Smaller base size that will be scaled
        textAlign = TextAlign.Center
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { pos ->
                        val originX = (size.width / 2f) + offsetX
                        val originY = size.height / 2f + offsetY
                        onTap(pos.x, pos.y, originX, originY)
                    },
                    onDoubleTap = {
                        onResetView()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { pos ->
                        val originX = size.width / 2f + offsetX
                        val originY = size.height / 2f + offsetY
                        onDragStart(pos.x, pos.y, originX, originY)
                    },
                    onDragEnd = {
                        onDragEnd()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        val originX = size.width / 2f + offsetX
                        val originY = size.height / 2f + offsetY

                        onDrag(
                            change.position.x,
                            change.position.y,
                            originX,
                            originY
                        )

                        // Let the parent decide whether this is point-drag or panning.
                        onPan(dragAmount.x, dragAmount.y)
                    }
                )
            }

    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                /*.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }*/
        ) {
            val nativeCanvas = drawContext.canvas.nativeCanvas
            val originX = size.width / 2f + offsetX
            val originY = size.height / 2f + offsetY

            // Calculate visible range in graph coordinates
            val scale = scale
            val minX = (-originX - offsetX) / (step * scale)
            val maxX = (size.width - originX - offsetX) / (step * scale)
            val minY = (originY + offsetY - size.height) / (step * scale)
            val maxY = (originY + offsetY) / (step * scale)

            // Calculate grid line step based on zoom level
            val gridStep = when {
                scale > 5f -> 0.5f
                scale > 2f -> 1f
                scale > 0.5f -> 2f
                else -> 5f
            }

            // Draw vertical grid lines
            val startX = (minX / gridStep).toInt() * gridStep - gridStep
            val endX = maxX + gridStep * 2
            var x1 = startX
            while (x1 <= endX) {
                val xPos = originX + x1 * step * scale
                if (xPos in -100f..(size.width + 100)) {
                    drawLine(
                        color = Color.DarkGray.copy(
                            alpha = if (x1.toInt().toFloat() == x1) 0.5f else 0.2f
                        ),
                        start = Offset(xPos, 0f),
                        end = Offset(xPos, size.height),
                        strokeWidth = if (x1.toInt().toFloat() == x1) 1f else 0.5f
                    )
                }
                x1 += gridStep
            }

            // Draw horizontal grid lines
            val startY = (minY / gridStep).toInt() * gridStep - gridStep
            val endY = maxY + gridStep * 2
            var y1 = startY
            while (y1 <= endY) {
                val yPos = originY - y1 * step * scale
                if (yPos in -100f..(size.height + 100)) {
                    drawLine(
                        color = Color.DarkGray.copy(
                            alpha = if (y1.toInt().toFloat() == y1) 0.5f else 0.2f
                        ),
                        start = Offset(0f, yPos),
                        end = Offset(size.width, yPos),
                        strokeWidth = if (y1.toInt().toFloat() == y1) 1f else 0.5f
                    )
                }
                y1 += gridStep
            }

            // --- Draw axes ---
            // X-axis
            drawLine(
                Color.Black,
                Offset(0f, originY),
                Offset(size.width, originY),
                strokeWidth = 2f * scale.coerceIn(0.5f, 2f)
            )

            // Y-axis
            drawLine(
                Color.Black,
                Offset(originX, 0f),
                Offset(originX, size.height),
                strokeWidth = 2f * scale.coerceIn(0.5f, 2f)
            )

            // --- Draw axis labels ---
            // X-axis labels
            var x = startX
            while (x <= endX) {
                if (x.toInt().toFloat() == x && x != 0f) {
                    val xPos = originX + x * step * scale
                    if (xPos in -100f..size.width - 100f) {
                        val text = x.toInt().toString()
                        val scaledTextStyle = baseTextStyle.copy(
                            fontSize = (16f * scale.coerceIn(0.5f, 2f)).sp
                        )
                        val textLayoutResult = textMeasurer.measure(
                            text = text,
                            style = scaledTextStyle,
                            maxLines = 1
                        )
                        val textOffset = Offset(
                            xPos - textLayoutResult.size.width / 2,
                            originY + 20f * scale.coerceIn(0.5f, 2f)
                        )
                        
                        if (textOffset.x in -100f..(size.width + 100) && 
                            textOffset.y in -100f..(size.height + 100)) {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = text,
                                style = scaledTextStyle,
                                topLeft = textOffset
                            )
                        }
                        // Using Compose's drawText for X-axis labels
                    }
                }
                x += gridStep
            }

            // Y-axis labels
            var y = startY
            while (y <= endY) {
                if (y.toInt().toFloat() == y && y != 0f) {
                    val yPos = originY - y * step * scale
                    if (yPos in -(size.height - 100f)..size.height - 100f) {
                        val text = y.toInt().toString()
                        val scaledTextStyle = baseTextStyle.copy(
                            fontSize = (16f * scale.coerceIn(0.5f, 2f)).sp
                        )
                        val textLayoutResult = textMeasurer.measure(
                            text = text,
                            style = scaledTextStyle,
                            maxLines = 1
                        )
                        val textOffset = Offset(
                            (originX - 24f * scale).coerceAtLeast(0f),
                            yPos + textLayoutResult.size.height / 2
                        )
                        
                        if (textOffset.x in -100f..(size.width + 100) && 
                            textOffset.y in -100f..(size.height + 100)) {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = text,
                                style = scaledTextStyle,
                                topLeft = textOffset
                            )
                        }
                    }
                }
                y += gridStep
            }

            // --- Draw axes ---
            // X-axis
            drawLine(
                Color.Black,
                Offset(0f, originY),
                Offset(size.width, originY),
                strokeWidth = 2f * scale.coerceIn(0.5f, 2f)
            )

            // Y-axis
            drawLine(
                Color.Black,
                Offset(originX, 0f),
                Offset(originX, size.height),
                strokeWidth = 2f * scale.coerceIn(0.5f, 2f)
            )

            // Origin label
            if (originX in -50f..50f && originY in -50f..50f) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "O",
                    style = baseTextStyle.copy(
                        fontSize = (16f * scale.coerceIn(0.5f, 2f)).sp
                    ),
                    topLeft = Offset(
                        originX - 16f * scale,
                        originY + 24f * scale
                    )
                )
            }

            drawExtra(this, originX, originY)
        }
        // Zoom controls
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .padding(8.dp)
        ) {
            // Zoom in button
            IconButton(
                onClick = {
                    onZoomIn(2f)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
            }

            // Zoom out button
            IconButton(
                onClick = {
                    onZoomOut(0.5f)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
            }

            // Reset view button
            TextButton(
                onClick = {
                    onResetView()
                },
                modifier = Modifier.size(48.dp)
            ) {
                Text("1:1", fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
    }
}
