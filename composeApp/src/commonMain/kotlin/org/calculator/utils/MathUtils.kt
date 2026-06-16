package org.calculator.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.calculator.ui.utils.ArcExpression
import org.calculator.ui.utils.ExpressionType
import org.calculator.ui.utils.Vector
import kotlin.math.*
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Coordinate helpers
// ---------------------------------------------------------------------------

fun cartesianToCanvas(
    x: Float, y: Float,
    originX: Float, originY: Float,
    step: Float, scale: Float
): Pair<Float, Float> =
    (originX + x * step * scale) to (originY - y * step * scale)

fun canvasToCartesian(
    canvasX: Float, canvasY: Float,
    originX: Float, originY: Float,
    step: Float, scale: Float
): Pair<Float, Float> {
    val x = (canvasX - originX) / (step * scale)
    val y = (originY - canvasY) / (step * scale)
    return x to y
}

// ---------------------------------------------------------------------------
// DrawScope helpers
// ---------------------------------------------------------------------------

/**
 * Renders a flat 1-D bitmap (0 = empty, non-zero = filled) as small circles.
 * [border] > 0 draws a contrasting outline circle before the fill circle.
 */
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
) {
    val width = scope.size.width.toInt()
    if (width <= 0) return
    for (i in points.indices) {
        if (points[i] == 0) continue
        val px = (i % width).toFloat()
        val py = (i / width).toFloat()
        if (border > 0f) {
            scope.drawCircle(
                center = Offset(px, py),
                radius = radius + border,
                color = borderColor
            )
        }
        scope.drawCircle(
            center = Offset(px, py),
            radius = radius,
            color = color
        )
    }
}

// ---------------------------------------------------------------------------
// Numerical integration (adaptive Simpson's rule)
// ---------------------------------------------------------------------------

fun calculateIntegral(
    function: (Float) -> Float,
    lowerLimit: Float,
    upperLimit: Float,
    eps: Double = 1e-4,
    maxRecursionDepth: Int = 20
): Float {
    if (lowerLimit.isNaN() || upperLimit.isNaN()) return Float.NaN
    if (lowerLimit == upperLimit) return 0f

    val sign = if (lowerLimit <= upperLimit) 1.0 else -1.0
    val a = minOf(lowerLimit, upperLimit).toDouble()
    val b = maxOf(lowerLimit, upperLimit).toDouble()

    fun f(x: Double): Double {
        val fx = function(x.toFloat())
        return fx.toDouble()
    }

    fun simpson(a: Double, b: Double, fa: Double, fb: Double, fm: Double) =
        (fa + 4.0 * fm + fb) * (b - a) / 6.0

    fun adaptiveSimpson(
        a: Double, b: Double,
        fa: Double, fb: Double, fm: Double,
        whole: Double, eps: Double, depth: Int
    ): Double {
        val m   = (a + b) / 2.0
        val lm  = (a + m) / 2.0
        val rm  = (m + b) / 2.0
        val flm = try { f(lm) } catch (_: Throwable) { Double.NaN }
        val frm = try { f(rm) } catch (_: Throwable) { Double.NaN }
        if (!flm.isFinite() || !frm.isFinite()) return Double.NaN
        val left  = simpson(a, m, fa, fm, flm)
        val right = simpson(m, b, fm, fb, frm)
        val delta = left + right - whole
        return if (depth <= 0 || abs(delta) <= 15.0 * eps) {
            left + right + delta / 15.0
        } else {
            val l = adaptiveSimpson(a, m, fa, fm, flm, left,  eps / 2.0, depth - 1)
            if (!l.isFinite()) return Double.NaN
            val r = adaptiveSimpson(m, b, fm, fb, frm, right, eps / 2.0, depth - 1)
            if (!r.isFinite()) return Double.NaN
            l + r
        }
    }

    val fa = try { f(a) } catch (_: Throwable) { Double.NaN }
    val fb = try { f(b) } catch (_: Throwable) { Double.NaN }
    val m  = (a + b) / 2.0
    val fm = try { f(m) } catch (_: Throwable) { Double.NaN }
    if (!fa.isFinite() || !fb.isFinite() || !fm.isFinite()) return Float.NaN

    val initial = simpson(a, b, fa, fb, fm)
    val result  = adaptiveSimpson(a, b, fa, fb, fm, initial, eps, maxRecursionDepth)
    return if (!result.isFinite()) Float.NaN else (sign * result).toFloat()
}

// ---------------------------------------------------------------------------
// Expression-type detection
// ---------------------------------------------------------------------------

/**
 * Detects the mathematical type of [formula].
 *
 * Order of checks matters — more specific patterns are tested first to avoid
 * false positives (e.g. parametric is gated on the explicit "r(t)=" prefix
 * OR a parenthesized pair that contains exactly one comma and no "=" to
 * prevent points and implicit equations from being mis-classified).
 */
fun formulaToExpressionType(formula: String): ExpressionType {
    val f = formula.lowercase().replace("\\s+".toRegex(), "")
    if (f.isEmpty()) return ExpressionType.CARTESIAN_Y_X

    // --- Explicit structured prefixes (highest priority) -----------------
    if (f.startsWith("integral("))           return ExpressionType.INTEGRAL
    if (f.startsWith("[") && f.endsWith("]")) return ExpressionType.AREA
    if (f.startsWith("arc("))               return ExpressionType.ARC
    if (f.startsWith("vec("))               return ExpressionType.VECTOR

    // --- Parametric: must start with "r(t)=" ---------------------------
    // We only accept the canonical form to avoid mis-classifying any
    // expression that happens to contain the letter 't'.
    if (f.startsWith("r(t)=")) return ExpressionType.PARAMETRIC

    // --- Polar ----------------------------------------------------------
    if (f.startsWith("r=") && f.contains("u")) return ExpressionType.POLAR_R_U
    if (f.startsWith("u="))                     return ExpressionType.POLAR_U

    // --- Cartesian vertical line: x = <number> -------------------------
    if (f.startsWith("x=") && !f.contains("y")) {
        val rest = f.substring(2)
        if (rest.toFloatOrNull() != null) return ExpressionType.CARTESIAN_X
    }

    // --- Cartesian explicit: y = f(x) ----------------------------------
    if (f.startsWith("y=") && !f.substring(2).contains("y")) {
        return ExpressionType.CARTESIAN_Y_X
    }

    // --- Point: (x,y) — exactly one comma, no equals -------------------
    if (f.startsWith("(") && f.endsWith(")") && !f.contains("=")) {
        val inner = f.drop(1).dropLast(1)
        // Ensure it is a bare coordinate pair, not a function call
        if (inner.count { it == ',' } == 1 && !inner.contains("(")) {
            return ExpressionType.POINT
        }
    }

    // --- Implicit: any equality remaining after the above ---------------
    if (f.contains("=")) return ExpressionType.CARTESIAN_IMPLICIT

    // --- Fallback implicit if x or y is present -------------------------
    if (f.contains("x") || f.contains("y")) return ExpressionType.CARTESIAN_IMPLICIT

    return ExpressionType.CARTESIAN_Y_X
}

// ---------------------------------------------------------------------------
// Geometry parsers
// ---------------------------------------------------------------------------

fun parseArc(formula: String): ArcExpression {
    // arc((cx,cy),r,start,sweep)
    val content = formula.trim()
        .removePrefix("arc(").removeSuffix(")")
    // Remove inner parens that wrap the center point
    val flat = content.replace("(", "").replace(")", "")
    val parts = flat.split(",").map { it.trim().toFloat() }
    require(parts.size == 5) { "arc() needs 5 numbers: cx,cy,r,start,sweep — got ${parts.size}" }
    return ArcExpression(parts[0], parts[1], parts[2], parts[3], parts[4])
}

fun parseVector(formula: String): Vector {
    val content = formula.trim()
        .removePrefix("vec(").removeSuffix(")")
        .replace("(", "").replace(")", "")
    val parts = content.split(",").map { it.trim().toFloat() }
    require(parts.size == 2) { "vec() needs 2 numbers" }
    return Vector(parts[0], parts[1])
}

fun parsePoint(formula: String): Vector {
    val inner = formula.trim().removeSurrounding("(", ")")
    val parts = inner.split(",").map { it.trim().toFloat() }
    return Vector(parts[0], parts[1])
}

fun parseArea(formula: String): List<String> =
    formula.removePrefix("[").removeSuffix("]").splitPoints().map { it.trim() }

/**
 * Splits a string of the form "(x1,y1),(x2,y2),..." into individual "(xi,yi)" tokens.
 */
fun String.splitPoints(): List<String> {
    val result = mutableListOf<String>()
    var depth = 0
    val current = StringBuilder()
    for (ch in this) {
        when (ch) {
            '(' -> { depth++; current.append(ch) }
            ')' -> {
                depth--; current.append(ch)
                if (depth == 0 && current.isNotEmpty()) {
                    result.add(current.toString())
                    current.clear()
                }
            }
            ',' -> if (depth > 0) current.append(ch)
            else -> if (depth > 0 || !ch.isWhitespace()) current.append(ch)
        }
    }
    return result
}

// ---------------------------------------------------------------------------
// Polar / Parametric evaluation helpers
// ---------------------------------------------------------------------------

/**
 * Evaluates a simple expression string that may contain only the variable [varName].
 * Supports: +, -, *, /, ^, sin, cos, tan, sqrt, abs, pi, e.
 * Returns [Double.NaN] on any parse or evaluation error.
 */
fun evalSimple(expr: String, varName: String, varValue: Double): Double = try {
    SimpleExprEval(expr.trim(), varName, varValue).eval()
} catch (_: Throwable) { Double.NaN }

// ---------------------------------------------------------------------------
// Arrow-head helper
// ---------------------------------------------------------------------------

fun arrowHeadPoints(
    endX: Float, endY: Float,
    originX: Float, originY: Float,
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
    val sinA = sin(angle); val cosA = cos(angle)
    val lx = cosA * ux - sinA * uy
    val ly = sinA * ux + cosA * uy
    val rx = cosA * ux + sinA * uy
    val ry = -sinA * ux + cosA * uy
    return listOf(
        endX - lx * arrowLength, endY - ly * arrowLength,
        endX - rx * arrowLength, endY - ry * arrowLength
    )
}

// ---------------------------------------------------------------------------
// Misc utilities
// ---------------------------------------------------------------------------

fun vecSum(vectors: List<Vector>, index: Int): Float =
    vectors.sumOf { (if (index == 0) it.x else it.y).toDouble() }.toFloat()

fun <T> List<T>.removeElement(index: Int): List<T> =
    subList(0, index) + subList(index + 1, size)

fun <T> List<T>.moveElement(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex == toIndex) return this
    val item = this[fromIndex]
    val listWithoutItem = this.toMutableList().apply { removeAt(fromIndex) }
    return listWithoutItem.apply { add(toIndex, item) }.toList()
}

fun Float.toRadians(): Float = (this * (PI / 180f)).toFloat()

/**
 * Returns a random, visually distinguishable color.
 * HSV: full saturation, random hue, value 0.65–0.90 to avoid near-black and
 * near-white outputs.
 */
fun randomRGBColor(): Color {
    val hue        = Random.nextFloat() * 360f
    val saturation = 0.75f + Random.nextFloat() * 0.25f   // 0.75 – 1.0
    val value      = 0.65f + Random.nextFloat() * 0.25f   // 0.65 – 0.90
    return Color.hsv(hue, saturation, value)
}
