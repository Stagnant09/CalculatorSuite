package org.calculator.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.calculator.ui.utils.ArcExpression
import org.calculator.ui.utils.ExpressionType
import org.calculator.ui.utils.Vector
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Converts a Cartesian coordinate (x, y) to the corresponding Compose canvas coordinate (Offset).
 *
 * It assumes the following variables are available in the current scope:
 * @param minX The minimum x-value in the data set.
 * @param rangeX The total span of the x-data (maxX - minX).
 * @param width The usable horizontal drawing space (excluding padding).
 * @param minY The minimum y-value in the data set.
 * @param rangeY The total span of the y-data (maxY - minY).
 * @param height The usable vertical drawing space (excluding padding).
 * @param padding The padding on all sides of the plotting area.
 */
fun scalePoint(
    x: Float,
    y: Float,
    minX: Float,
    rangeX: Float,
    width: Float,
    minY: Float,
    rangeY: Float,
    height: Float,
    padding: Float
): Offset {
    // X-axis scaling is correct: increases from left (padding) to right (padding + width).
    val scaledX = padding + ((x - minX) / rangeX) * width

    // Y-axis correction:
    // 1. Calculate the scaled position (0 to height)
    //    This still gives 0 for minY and 'height' for maxY.
    val scaledYFromMin = ((y - minY) / rangeY) * height

    // 2. Invert the Y value relative to the plotting area height.
    //    Since canvas Y increases downwards, (0, -1) should map to a high canvas Y value.
    //    We subtract the scaled value from the total height, then add the top padding.
    //    This correctly maps minY (lowest Cartesian) to padding + height (highest canvas Y)
    //    and maxY (highest Cartesian) to padding (lowest canvas Y).
    val invertedScaledY = padding + (height - scaledYFromMin)

    return Offset(scaledX, invertedScaledY)
}

/**
 * Numerically integrate `function` from `lowerLimit` to `upperLimit`.
 * Uses adaptive Simpson's rule (recursive). Returns Float (or Float.NaN on error).
 *
 * Note: Uses Double internally for accuracy, then casts to Float.
 */
fun calculateIntegral(
    function: (Float) -> Float,
    lowerLimit: Float,
    upperLimit: Float,
    eps: Double = 1e-4,         // requested absolute error
    maxRecursionDepth: Int = 20 // prevents runaway recursion
): Float {
    // quick checks
    if (lowerLimit.isNaN() || upperLimit.isNaN()) return Float.NaN
    if (lowerLimit == upperLimit) return 0f

    // allow reversed limits
    val sign = if (lowerLimit <= upperLimit) 1.0 else -1.0
    val a = minOf(lowerLimit, upperLimit).toDouble()
    val b = maxOf(lowerLimit, upperLimit).toDouble()

    // wrapper to call the user's function with double precision
    fun f(x: Double): Double {
        val fx = function(x.toFloat())
        return fx.toDouble()
    }

    // Simpson estimate on [a, b]
    fun simpson(a: Double, b: Double, fa: Double, fb: Double, fm: Double): Double {
        return (fa + 4.0 * fm + fb) * (b - a) / 6.0
    }

    // Adaptive Simpson recursion
    fun adaptiveSimpson(
        a: Double,
        b: Double,
        fa: Double,
        fb: Double,
        fm: Double,
        whole: Double,
        eps: Double,
        depth: Int
    ): Double {
        val m = (a + b) / 2.0
        val lm = (a + m) / 2.0
        val rm = (m + b) / 2.0

        val flm = try { f(lm) } catch (e: Throwable) { Double.NaN }
        val frm = try { f(rm) } catch (e: Throwable) { Double.NaN }

        // If function produced NaN or infinite values, bail out
        if (!flm.isFinite() || !frm.isFinite()) return Double.NaN

        val left = simpson(a, m, fa, fm, flm)
        val right = simpson(m, b, fm, fb, frm)
        val delta = left + right - whole

        // If good enough or max depth reached, return corrected estimate
        return if (depth <= 0 || abs(delta) <= 15.0 * eps) {
            // Richardson extrapolation
            left + right + delta / 15.0
        } else {
            // Recurse on left and right halves with half tolerance
            val leftRes = adaptiveSimpson(a, m, fa, fm, flm, left, eps / 2.0, depth - 1)
            if (!leftRes.isFinite()) return Double.NaN
            val rightRes = adaptiveSimpson(m, b, fm, fb, frm, right, eps / 2.0, depth - 1)
            if (!rightRes.isFinite()) return Double.NaN
            leftRes + rightRes
        }
    }

    // initial function evaluations
    val fa = try { f(a) } catch (e: Throwable) { Double.NaN }
    val fb = try { f(b) } catch (e: Throwable) { Double.NaN }
    val m = (a + b) / 2.0
    val fm = try { f(m) } catch (e: Throwable) { Double.NaN }

    if (!fa.isFinite() || !fb.isFinite() || !fm.isFinite()) return Float.NaN

    val initial = simpson(a, b, fa, fb, fm)
    val result = adaptiveSimpson(a, b, fa, fb, fm, initial, eps, maxRecursionDepth)

    return if (!result.isFinite()) Float.NaN else (sign * result).toFloat()
}

val floatRegex = Regex("^[-]?\\d*\\.?\\d+$")

fun cartesianToCanvas(x: Float, y: Float, originX: Float, originY: Float, step: Float, scale: Float) : Pair<Float, Float>{
    return Pair(originX + x * step * scale, originY - y * step * scale)
}

fun canvasToCartesian(
    canvasX: Float,
    canvasY: Float,
    originX: Float,
    originY: Float,
    step: Float,
    scale: Float
): Pair<Float, Float> {
    val x = (canvasX - originX) / (step * scale)
    val y = (originY - canvasY) / (step * scale)
    return x to y
}

fun drawPoints(
    points: IntArray,
    scope: DrawScope,
    originX: Float,
    originY: Float,
    step: Float,
    scale: Float,
    color: Color,
    centerX: Float,
    centerY: Float,
    radius: Float = 1.5f,
    border: Float = 0f,
    borderColor: Color = Color.Black
){
    for (i in points.indices) {
        if (points[i] == 0) continue
        val px = i % 500
        val py = i / 500
        val worldX = (px - centerX) / (step * scale)
        val worldY = (centerY - py) / (step * scale)
        val coords = cartesianToCanvas(worldX, worldY, originX, originY, 40f, scale)
        println("Drawing circle at ($worldX, $worldY) of radius $radius")
        if (border > 0f) {
            scope.drawCircle(
                center = Offset(
                    coords.first,
                    coords.second
                ),
                radius = radius + border,
                color = borderColor
            )
        }
        scope.drawCircle(
            center = Offset(
                coords.first,
                coords.second
            ),
            radius = radius,
            color = color
        )
    }
}

/** Detects the type of expression from the formula
 * @param formula The formula to detect the type of
 * @return An `ExpressionType`
 */
fun formulaToExpressionType(formula: String): ExpressionType {
    val f = formula
        .lowercase()
        .replace("\\s+".toRegex(), "")

    /* ---------- Explicit structured constructs ---------- */

    // Integral(f(x), a, b)
    if (f.startsWith("integral(")) {
        return ExpressionType.INTEGRAL
    }

    // Area: [(x1,y1),(x2,y2),...]
    if (f.startsWith("[") && f.contains("),(")) {
        return ExpressionType.AREA
    }

    // Arc: arc((x,y),r,u)
    if (f.startsWith("arc(")) {
        return ExpressionType.ARC
    }

    // Vector: vec(x,y)
    if (f.startsWith("vec(")) {
        return ExpressionType.VECTOR
    }

    // Point: (x,y)
    if (f.startsWith("(") && f.endsWith(")") && f.contains(",")) {
        return ExpressionType.POINT
    }

    /* ---------- Parametric ---------- */

    // x(t), y(t) or r(t)
    if (f.contains("t")) {
        return ExpressionType.PARAMETRIC
    }

    /* ---------- Polar ---------- */

    // r = f(u)
    if (f.contains("r=") && f.contains("u")) {
        return ExpressionType.POLAR_R_U
    }

    // u = c
    if (f.startsWith("u=")) {
        return ExpressionType.POLAR_U
    }

    /* ---------- Cartesian ---------- */

    // y = f(x)
    if (f.startsWith("y=")) {
        return ExpressionType.CARTESIAN_Y_X
    }

    // x = c
    if (f.startsWith("x=") && !f.contains("y")) {
        return ExpressionType.CARTESIAN_X
    }

    // f(x,y) = 0
    if (f.contains("x") && f.contains("y") && f.contains("=")) {
        return ExpressionType.CARTESIAN_IMPLICIT
    }

    /* ---------- Fallback ---------- */

    // Default to implicit Cartesian if x or y exists
    if (f.contains("x") || f.contains("y")) {
        return ExpressionType.CARTESIAN_IMPLICIT
    }

    // Safe fallback
    return ExpressionType.CARTESIAN_Y_X
}

fun parseArc(formula: String): ArcExpression {
    val content = formula
        .removePrefix("arc(")
        .removeSuffix(")")

    val parts = content
        .replace("(", "")
        .replace(")", "")
        .split(",")
        .map { it.trim().toFloat() }

    require(parts.size == 5)

    return ArcExpression(
        x = parts[0],
        y = parts[1],
        r = parts[2],
        start = parts[3],
        sweep = parts[4]
    )
}

fun parseVector(formula: String): Vector {
    val content = formula
        .removePrefix("vec(")
        .removeSuffix(")")

    val parts = content
        .replace("(", "")
        .replace(")", "")
        .split(",")
        .map { it.trim().toFloat() }

    require(parts.size == 2)

    return Vector(
        x = parts[0],
        y = parts[1]
    )
}

fun arrowHeadPoints(
    endX: Float,
    endY: Float,
    originX: Float,
    originY: Float,
    arrowLength: Float = 20f,
    angleDeg: Float = 30f
): List<Float> {

    val dx = endX - originX
    val dy = endY - originY

    val len = hypot(dx, dy)
    if (len == 0f) return listOf(endX, endY, endX, endY)

    val ux = dx / len
    val uy = dy / len

    val angle = angleDeg.toRadians()
    val sin = sin(angle)
    val cos = cos(angle)

    // Rotate BACKWARD from direction (arrowhead points backward)
    val lx = cos * ux - sin * uy
    val ly = sin * ux + cos * uy

    val rx = cos * ux + sin * uy
    val ry = -sin * ux + cos * uy

    return listOf(
        endX - lx * arrowLength,
        endY - ly * arrowLength,
        endX - rx * arrowLength,
        endY - ry * arrowLength
    )
}


fun Float.toRadians(): Float {
    return (this * (PI / 180f)).toFloat()
}
