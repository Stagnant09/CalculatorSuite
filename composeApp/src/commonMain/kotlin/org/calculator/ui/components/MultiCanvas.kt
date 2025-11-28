package org.calculator.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.calculator.utils.cartesianToCanvas
import kotlin.math.roundToInt

@Composable
fun MultiCanvas() {
    // State for scale and offset
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

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
        // Example: Draw a red dot at (1,1)
        val point = cartesianToCanvas(1f, 1f, originX, originY, step, scale)
        scope.drawCircle(
            color = Color.Red,
            radius = 5f,
            center = Offset(
                point.first,
                point.second
            )
        )
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
            drawExtra = drawExtra
        )
    }

}