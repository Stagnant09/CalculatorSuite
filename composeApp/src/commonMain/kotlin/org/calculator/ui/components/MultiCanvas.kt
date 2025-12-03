package org.calculator.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.calculator.nativeLib.ImplicitPlotter
import org.calculator.ui.utils.Expression
import org.calculator.utils.canvasToCartesian
import org.calculator.utils.cartesianToCanvas
import org.calculator.utils.drawPoints
import kotlin.math.roundToInt

@Composable
fun MultiCanvas(
    expressions: List<Expression>,
    colors: List<Color>
) {
    // State for scale and offset
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val textMeasurer = rememberTextMeasurer()
    val cursorX = remember { mutableStateOf(0f) }
    val cursorY = remember { mutableStateOf(0f) }
    val currentPos = remember { mutableStateOf(Pair(0f, 0f)) }
    val label = "(${currentPos.value.first}, ${currentPos.value.second})"
    // Grid step (distance between grid lines in pixels at scale 1.0)
    val step = 40f

    // Handle panning
    val onPan = { dx: Float, dy: Float ->
        offsetX += dx
        offsetY += dy
    }

    // Handle zooming
    val onZoomIn: (Float) -> Unit = { factor ->
        scale = (scale * factor).coerceIn(0.1f, 10f)
    }

    val onZoomOut: (Float) -> Unit = { factor ->
        scale = (scale * factor).coerceIn(0.1f, 10f)
    }

    // Reset view
    val onResetView = {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    // Handle tap
    val onTap = { x: Float, y: Float, originX: Float, originY: Float ->
        // Convert screen coordinates to graph coordinates
        val graphX = ((x - originX) / (step * scale)).roundToInt().toFloat()
        val graphY = -((y - originY) / (step * scale)).roundToInt().toFloat()
        println("Tapped at graph coordinates: ($graphX, $graphY)")
    }

    // Handle drag
    var isDragging by remember { mutableStateOf(false) }

    val onDragStart = { x: Float, y: Float, originX: Float, originY: Float ->
        isDragging = true
    }

    val onDragEnd = {
        isDragging = false
    }

    val onDrag = { x: Float, y: Float, originX: Float, originY: Float ->
        if (isDragging) {
            // Handle dragging logic here if needed
        }
    }

    // Custom drawing function for the grid
    val drawExtra: (DrawScope, Float, Float) -> Unit = { scope, originX, originY ->
        expressions.forEachIndexed { index, expression ->
            val plotter = ImplicitPlotter()
            plotter.setFormula(expression.formula)
            val bitmapWidth = 500
            val bitmapHeight = 500
            val centerX = bitmapWidth / 2f
            val centerY = bitmapHeight / 2f
            val bitmap = plotter.evaluateBitmap(
                width = bitmapWidth,
                height = bitmapHeight,
                originX = centerX,
                originY = centerY,
                step = step,
                scale = scale,
                threshold = 0.012f  // lower threshold for better sampling
            )
            drawPoints(
                points = bitmap,
                scope = scope,
                originX = originX,
                originY = originY,
                step = step,
                scale = scale,
                color = colors[index],
                centerX = centerX,
                centerY = centerY
            )
            // Find meet points w/ all previous functions
            if (index > 0) {
                for (j in 0..<index) {
                    plotter.setFormula2(expressions[j].formula)
                    val meetPoints = plotter.meetPoints(
                        width = bitmapWidth,
                        height = bitmapHeight,
                        originX = centerX,
                        originY = centerY,
                        step = step,
                        scale = scale,
                        threshold = 0.0000005f  // lower threshold for better sampling
                    )
                    drawPoints(
                        points = meetPoints,
                        scope = scope,
                        originX = originX,
                        originY = originY,
                        step = step,
                        scale = scale,
                        color = Color.Yellow,
                        centerX = centerX,
                        centerY = centerY,
                        radius = 5f,
                        border = 2f,
                        borderColor = Color.Black
                    )
                }
            }
        }
        // Compute cursor position in Cartesian coordinates
        val currentPos1 = canvasToCartesian(
            canvasX = cursorX.value,
            canvasY = cursorY.value,
            originX = originX,
            originY = originY,
            step = step,
            scale = scale
        )
        currentPos.value = Pair(currentPos1.first, currentPos1.second)
        // Convert dp/sp → px manually for canvas
        val paint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = Color.Black.toArgb()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CartesianGridCanvas(
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            step = step,
            onPan = onPan,
            onZoomIn = onZoomIn,
            onZoomOut = onZoomOut,
            onResetView = onResetView,
            onTap = onTap,
            onDragStart = onDragStart,
            onDragEnd = onDragEnd,
            onDrag = onDrag,
            drawExtra = drawExtra,
            pointerInputExtra = { newCursorX, newCursorY ->
                cursorX.value = newCursorX
                cursorY.value = newCursorY
            }
        )
        // Draw label at a stable offset (bottom-right for example)
        Text(
            text = label,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

}