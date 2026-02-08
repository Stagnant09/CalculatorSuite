package org.calculator.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import org.calculator.nativeLib.ImplicitPlotter
import org.calculator.ui.utils.Expression
import org.calculator.ui.utils.Vector
import org.calculator.utils.*
import kotlin.math.atan2
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
            when (expression) {
                is Expression.CartesianYXExpression,
                is Expression.CartesianXExpression,
                is Expression.CartesianImplicitExpression,
                     -> {
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
                            when (expressions[j]) {
                                is Expression.CartesianYXExpression,
                                is Expression.CartesianXExpression,
                                is Expression.CartesianImplicitExpression -> {
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
                                else -> {
                                    // Do nothing
                                }
                            }
                        }
                    }
                }

                is Expression.ArcExpression -> {
                    val arc = parseArc(expressions[index].formula)

                    val (canvasX, canvasY) = cartesianToCanvas(
                        x = arc.x,
                        y = arc.y,
                        originX = originX,
                        originY = originY,
                        step = step,
                        scale = scale
                    )

                    val radius = arc.r * step * scale

                    val topLeft = Offset(
                        canvasX - radius,
                        canvasY - radius
                    )

                    scope.drawArc(
                        color = colors[index],
                        startAngle = -arc.start,
                        sweepAngle = -arc.sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 2f)
                    )
                }

                is Expression.AreaExpression -> {
                    try {
                        val points : MutableList<Vector> = mutableListOf()
                        parseArea(expression.formula).forEach { pointString ->
                            println(pointString)
                            points.add(parsePoint(pointString))
                        }
                        points.forEach { point ->
                            val (x, y) = point
                            val (canvasX, canvasY) = cartesianToCanvas(
                                x = x,
                                y = y,
                                originX = originX,
                                originY = originY,
                                step = step,
                                scale = scale
                            )
                            scope.drawCircle(
                                center = Offset(canvasX, canvasY),
                                radius = 5f,
                                color = colors[index],
                                style = Fill
                            )
                        }
                        // Shade the area enclosed by the points
                        if (points.size >= 3) {
                            // Sort points to form a proper polygon (counterclockwise from centroid)
                            val centroid = Vector(
                                x = vecSum(points, 0) / points.size,
                                y = vecSum(points, 1) / points.size
                            )

                            val sortedPoints = points.sortedBy { point ->
                                atan2(point.y - centroid.y, point.x - centroid.x)
                            }

                            val path = Path().apply {
                                val firstPoint = sortedPoints.first()
                                val (startX, startY) = cartesianToCanvas(
                                    x = firstPoint.x,
                                    y = firstPoint.y,
                                    originX = originX,
                                    originY = originY,
                                    step = step,
                                    scale = scale
                                )
                                moveTo(startX, startY)

                                sortedPoints.drop(1).forEach { point ->
                                    val (x, y) = point
                                    val (canvasX, canvasY) = cartesianToCanvas(
                                        x = x,
                                        y = y,
                                        originX = originX,
                                        originY = originY,
                                        step = step,
                                        scale = scale
                                    )
                                    lineTo(canvasX, canvasY)
                                }
                                close()
                            }

                            scope.drawPath(
                                path = path,
                                color = colors[index].copy(alpha = 0.3f),
                                style = Fill
                            )
                        }
                    }
                    catch (e: Exception) {}
                }
                is Expression.IntegralExpression -> {}
                is Expression.ParametricExpression -> {}
                is Expression.PointExpression -> {
                    val (x, y) = expression.formula.removeSurrounding("(", ")").split(",").map { it.trim().toFloat() }
                    val (canvasX, canvasY) = cartesianToCanvas(
                        x = x,
                        y = y,
                        originX = originX,
                        originY = originY,
                        step = step,
                        scale = scale
                    )
                    scope.drawCircle(
                        center = Offset(canvasX, canvasY),
                        radius = 5f,
                        color = colors[index],
                        style = Fill
                    )
                }
                is Expression.PolarRUExpression -> {}
                is Expression.PolarUExpression -> {}
                is Expression.VectorExpression -> {
                    val vector = parseVector(expressions[index].formula)
                    val (canvasX, canvasY) = cartesianToCanvas(
                        x = vector.x,
                        y = vector.y,
                        originX = originX,
                        originY = originY,
                        step = step,
                        scale = scale
                    )
                    scope.drawLine(
                        start = Offset(originX, originY),
                        end = Offset(canvasX, canvasY),
                        color = colors[index],
                        strokeWidth = 4f
                    )
                    // Draw arrowhead
                    val arrow = arrowHeadPoints(
                        endX = canvasX,
                        endY = canvasY,
                        originX = originX,
                        originY = originY
                    )

                    scope.drawLine(
                        start = Offset(canvasX, canvasY),
                        end = Offset(arrow[0], arrow[1]),
                        color = colors[index],
                        strokeWidth = 4f
                    )

                    scope.drawLine(
                        start = Offset(canvasX, canvasY),
                        end = Offset(arrow[2], arrow[3]),
                        color = colors[index],
                        strokeWidth = 4f
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
        // Draw label at a stable offset (bottom-right)
        Text(
            text = label,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

}