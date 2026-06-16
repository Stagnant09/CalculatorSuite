package org.calculator.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.calculator.nativeLib.ImplicitPlotter
import org.calculator.ui.utils.Expression
import org.calculator.ui.utils.Vector
import org.calculator.utils.*
import kotlin.math.*

@Composable
fun MultiCanvas(
    expressions: List<Expression>,
    colors: List<Color>,
    thicknesses: List<Float>,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onViewportChange: (Float, Float, Float) -> Unit
) {
    val step = 40f

    // -----------------------------------------------------------------------
    // Hoist ImplicitPlotter outside the draw lambda so it is not re-created
    // on every frame.  One instance is sufficient for the JVM target; the
    // native library itself holds global state per-formula slot.
    // -----------------------------------------------------------------------
    val plotter = remember { ImplicitPlotter() }

    val cursorPos = remember { mutableStateOf(Pair(0f, 0f)) }

    val label = remember(cursorPos.value) {
        val (cx, cy) = cursorPos.value
        val fx = (cx * 100).roundToLong() / 100f
        val fy = (cy * 100).roundToLong() / 100f
        "($fx, $fy)"
    }

    // -----------------------------------------------------------------------
    // drawExtra — called inside Canvas{} with the computed origin
    // -----------------------------------------------------------------------
    val drawExtra: (DrawScope, Float, Float) -> Unit = { scope, originX, originY ->

        expressions.forEachIndexed { index, expression ->
            val color = colors.getOrElse(index) { Color.Gray }

            when (expression) {
                // -----------------------------------------------------------
                // Cartesian (explicit, vertical, implicit) — native C++ path
                // -----------------------------------------------------------
                is Expression.CartesianYXExpression,
                is Expression.CartesianXExpression,
                is Expression.CartesianImplicitExpression -> {
                    val bw = scope.size.width.toInt()
                    val bh = scope.size.height.toInt()
                    // threshold scales with zoom so thin implicit curves stay
                    // visible when zoomed out and don't bloat when zoomed in
                    val threshold = (0.025f / scale.coerceAtLeast(0.5f))

                    plotter.setFormula(expression.formula)
                    val bitmap = plotter.evaluateBitmap(bw, bh, originX, originY, step, scale, threshold)
                    drawPoints(
                        bitmap,
                        scope,
                        originX,
                        originY,
                        step,
                        scale,
                        color,
                        originX,
                        originY,
                        radius = thicknesses.getOrElse(index) { 1.5f })

                    // Intersection points with all earlier Cartesian curves
                    if (index > 0) {
                        for (j in 0 until index) {
                            val other = expressions[j]
                            if (other is Expression.CartesianYXExpression ||
                                other is Expression.CartesianXExpression ||
                                other is Expression.CartesianImplicitExpression
                            ) {
                                plotter.setFormula2(other.formula)
                                val meet = plotter.meetPoints(
                                    bw, bh, originX, originY, step, scale,
                                    threshold = 0.0000005f
                                )
                                drawPoints(
                                    meet, scope, originX, originY, step, scale,
                                    color = Color.Yellow,
                                    centerX = originX, centerY = originY,
                                    radius = 5f,
                                    border = 2f,
                                    borderColor = Color.Black
                                )
                            }
                        }
                    }
                }

                // -----------------------------------------------------------
                // Polar  r = f(u)
                // -----------------------------------------------------------
                is Expression.PolarRUExpression -> {
                    val lhs = expression.lhs
                    val rhs = expression.rhs
                    val steps = 2000
                    val uMin = 0.0
                    val uMax = 4 * PI
                    var prevCanvas: Pair<Float, Float>? = null

                    for (i in 0..steps) {
                        val u = uMin + (uMax - uMin) * i / steps

                        // We need to find r such that evaluate(lhs, r, u) = evaluate(rhs, r, u)
                        // Actually, r is what we want. 
                        // If it's "2r = sin(u)", then 2*r - sin(u) = 0 => r = sin(u)/2

                        val r = if (lhs == "r") {
                            evalSimple(rhs, "u", u)
                        } else {
                            // General case: lhs(r, u) = rhs(r, u)
                            // We solve f(r) = eval(lhs, r, u) - eval(rhs, r, u) = 0
                            val f = { rVal: Double ->
                                val l = evalSimple(lhs, "r", rVal, "u", u)
                                val rV = evalSimple(rhs, "r", rVal, "u", u)
                                l - rV
                            }
                            solveForR(f)
                        }

                        if (!r.isFinite()) {
                            prevCanvas = null; continue
                        }
                        val cx = r * cos(u)
                        val cy = r * sin(u)
                        val (px, py) = cartesianToCanvas(cx.toFloat(), cy.toFloat(), originX, originY, step, scale)
                        val cur = px to py
                        prevCanvas?.let { (lx, ly) ->
                            scope.drawLine(
                                color = color,
                                start = Offset(lx, ly),
                                end = Offset(px, py),
                                strokeWidth = 2f
                            )
                        }
                        prevCanvas = cur
                    }
                }

                // -----------------------------------------------------------
                // Polar  u = constant  (radial line)
                // -----------------------------------------------------------
                is Expression.PolarUExpression -> {
                    val angle = expression.formula.substringAfter("=").trim().toDoubleOrNull()
                        ?: evalSimple(expression.formula.substringAfter("=").trim(), "u", 0.0)
                    if (angle.isFinite()) {
                        val far = 1e4f
                        val (ex, ey) = cartesianToCanvas(
                            (far * cos(angle)).toFloat(),
                            (far * sin(angle)).toFloat(),
                            originX, originY, step, scale
                        )
                        scope.drawLine(
                            color = color,
                            start = Offset(originX, originY),
                            end = Offset(ex, ey),
                            strokeWidth = 2f
                        )
                    }
                }

                // -----------------------------------------------------------
                // Parametric  r(t) = (x(t), y(t))
                // -----------------------------------------------------------
                is Expression.ParametricExpression -> {
                    val xExpr = expression.xFormula
                    val yExpr = expression.yFormula
                    val steps = 2000
                    val tMin = -2 * PI
                    val tMax = 2 * PI
                    var prevCanvas: Pair<Float, Float>? = null

                    for (i in 0..steps) {
                        val t = tMin + (tMax - tMin) * i / steps
                        val cx = evalSimple(xExpr, "t", t)
                        val cy = evalSimple(yExpr, "t", t)
                        if (!cx.isFinite() || !cy.isFinite()) {
                            prevCanvas = null; continue
                        }
                        val (px, py) = cartesianToCanvas(cx.toFloat(), cy.toFloat(), originX, originY, step, scale)
                        prevCanvas?.let { (lx, ly) ->
                            scope.drawLine(
                                color = color,
                                start = Offset(lx, ly),
                                end = Offset(px, py),
                                strokeWidth = 2f
                            )
                        }
                        prevCanvas = px to py
                    }
                }

                // -----------------------------------------------------------
                // Integral — shade the area under f(x) between a and b
                // -----------------------------------------------------------
                is Expression.IntegralExpression -> {
                    // Parse the integrand and evaluate with the Kotlin evaluator
                    val funcExpr = expression.function.trim()
                    val a = expression.lowerLimit
                    val b = expression.upperLimit
                    if (a.isFinite() && b.isFinite()) {
                        val steps = 800
                        val lo = minOf(a, b)
                        val hi = maxOf(a, b)
                        val xValues = (0..steps).map { lo + (hi - lo) * it / steps }
                        val yValues = xValues.map { x ->
                            evalSimple(funcExpr, "x", x.toDouble()).toFloat()
                        }

                        // Build a filled polygon: curve on top, baseline on bottom
                        val path = Path()
                        var started = false
                        xValues.zip(yValues).forEach { (x, y) ->
                            if (y.isFinite()) {
                                val (px, py) = cartesianToCanvas(x, y, originX, originY, step, scale)
                                if (!started) {
                                    path.moveTo(px, py); started = true
                                } else path.lineTo(px, py)
                            }
                        }
                        if (started) {
                            val (bx, _) = cartesianToCanvas(hi, 0f, originX, originY, step, scale)
                            val (ax, _) = cartesianToCanvas(lo, 0f, originX, originY, step, scale)
                            path.lineTo(bx, originY)
                            path.lineTo(ax, originY)
                            path.close()
                            scope.drawPath(path, color.copy(alpha = 0.35f), style = Fill)
                            scope.drawPath(path, color, style = Stroke(width = 2f))
                        }

                        // Also draw the integrand curve for context
                        var prev: Pair<Float, Float>? = null
                        xValues.zip(yValues).forEach { (x, y) ->
                            if (y.isFinite()) {
                                val cur = cartesianToCanvas(x, y, originX, originY, step, scale)
                                prev?.let { (lx, ly) ->
                                    scope.drawLine(
                                        color = color,
                                        start = Offset(lx, ly),
                                        end = Offset(cur.first, cur.second),
                                        strokeWidth = 2f
                                    )
                                }
                                prev = cur
                            } else prev = null
                        }
                    }
                }

                // -----------------------------------------------------------
                // Point
                // -----------------------------------------------------------
                is Expression.PointExpression -> {
                    val (px, py) = cartesianToCanvas(expression.x, expression.y, originX, originY, step, scale)
                    // White border then filled dot
                    scope.drawCircle(Color.White, radius = 8f, center = Offset(px, py))
                    scope.drawCircle(color, radius = 6f, center = Offset(px, py))
                    scope.drawCircle(Color.Black, radius = 6f, center = Offset(px, py), style = Stroke(1.5f))
                }

                // -----------------------------------------------------------
                // Vector
                // -----------------------------------------------------------
                is Expression.VectorExpression -> {
                    val (ex, ey) = cartesianToCanvas(expression.x, expression.y, originX, originY, step, scale)
                    scope.drawLine(
                        color = color,
                        start = Offset(originX, originY),
                        end = Offset(ex, ey),
                        strokeWidth = 3f
                    )
                    val arrow = arrowHeadPoints(ex, ey, originX, originY)
                    scope.drawLine(
                        color = color,
                        start = Offset(ex, ey),
                        end = Offset(arrow[0], arrow[1]),
                        strokeWidth = 3f
                    )
                    scope.drawLine(
                        color = color,
                        start = Offset(ex, ey),
                        end = Offset(arrow[2], arrow[3]),
                        strokeWidth = 3f
                    )
                }

                // -----------------------------------------------------------
                // Arc
                // -----------------------------------------------------------
                is Expression.ArcExpression -> {
                    val arc = parseArc(expression.formula)
                    val (cx, cy) = cartesianToCanvas(arc.x, arc.y, originX, originY, step, scale)
                    val r = arc.r * step * scale
                    scope.drawArc(
                        color = color,
                        startAngle = -arc.start,
                        sweepAngle = -arc.sweep,
                        useCenter = false,
                        topLeft = Offset(cx - r, cy - r),
                        size = Size(r * 2, r * 2),
                        style = Stroke(width = 2f)
                    )
                }

                // -----------------------------------------------------------
                // Area polygon
                // -----------------------------------------------------------
                is Expression.AreaExpression -> {
                    try {
                        val pts = parseArea(expression.formula).map { parsePoint(it) }
                        // Draw vertex dots
                        pts.forEach { (x, y) ->
                            val (px, py) = cartesianToCanvas(x, y, originX, originY, step, scale)
                            scope.drawCircle(color, 4f, Offset(px, py))
                        }
                        if (pts.size >= 3) {
                            val centroid = Vector(vecSum(pts, 0) / pts.size, vecSum(pts, 1) / pts.size)
                            val sorted = pts.sortedBy { atan2(it.y - centroid.y, it.x - centroid.x) }
                            val path = Path()
                            sorted.forEachIndexed { i, (x, y) ->
                                val (px, py) = cartesianToCanvas(x, y, originX, originY, step, scale)
                                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                            }
                            path.close()
                            scope.drawPath(path, color.copy(alpha = 0.28f), style = Fill)
                            scope.drawPath(path, color, style = Stroke(2f))
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CartesianGridCanvas(
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            step = step,
            onPan = { dx, dy -> onViewportChange(scale, offsetX + dx, offsetY + dy) },
            onZoom = { factor, focalX, focalY ->
                val newScale = (scale * factor).coerceIn(0.1f, 100f)
                // Zoom toward the focal point (mouse cursor)
                val newOffsetX = focalX + (offsetX - focalX) * (newScale / scale)
                val newOffsetY = focalY + (offsetY - focalY) * (newScale / scale)
                onViewportChange(newScale, newOffsetX, newOffsetY)
            },
            onResetView = { onViewportChange(1f, 0f, 0f) },
            drawExtra = drawExtra,
            onCursorMoved = { canvasX, canvasY, originX, originY ->
                cursorPos.value = canvasToCartesian(canvasX, canvasY, originX, originY, step, scale)
            }
        )
        Text(
            text = label,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun Double.roundToLong() = kotlin.math.round(this).toLong()
