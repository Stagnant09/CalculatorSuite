package org.calculator.ui.utils

enum class ExpressionType {
    CARTESIAN_Y_X,        // y = f(x)
    CARTESIAN_X,          // x = c  (vertical line)
    CARTESIAN_IMPLICIT,   // f(x,y) = 0
    POLAR_R_U,            // r = f(u)
    POLAR_U,              // u = c  (radial line)
    PARAMETRIC,           // r(t) = (x(t), y(t))
    POINT,                // (x,y)
    INTEGRAL,             // integral(f(x), a, b)
    AREA,                 // [(x1,y1),(x2,y2),…]
    VECTOR,               // vec(x,y)
    ARC                   // arc((cx,cy),r,start,sweep)
}

/** Data class returned by parseArc() — not an Expression itself */
data class ArcExpression(
    val x: Float,
    val y: Float,
    val r: Float,
    val start: Float,
    val sweep: Float
)

data class Vector(val x: Float, val y: Float)

// ---------------------------------------------------------------------------
// Expression sealed hierarchy
// ---------------------------------------------------------------------------

sealed interface Expression {
    val formula: String

    data class CartesianYXExpression(override val formula: String) : Expression

    data class CartesianXExpression(override val formula: String) : Expression {
        /** The constant c in  x = c. Defaults to 0 if unparseable. */
        val constant: Float = formula.substringAfter("=").trim().toFloatOrNull() ?: 0f
    }

    data class CartesianImplicitExpression(override val formula: String) : Expression

    data class PolarRUExpression(override val formula: String) : Expression {
        val lhs: String
        val rhs: String

        init {
            val parts = formula.split("=")
            if (parts.size >= 2) {
                lhs = parts[0].trim()
                rhs = parts.drop(1).joinToString("=").trim()
            } else {
                lhs = "r"
                rhs = formula.trim()
            }
        }
    }

    data class PolarUExpression(override val formula: String) : Expression {
        val constant: Float = formula.substringAfter("=").trim().toFloatOrNull() ?: 0f
    }

    /**
     * Parametric curve.  Expected formula form:  r(t) = (xExpr, yExpr)
     * where xExpr and yExpr are expressions in t.
     *
     * The RHS is split on the FIRST top-level comma inside the outer
     * parentheses so that expressions like "cos(2*t), sin(3*t)" are parsed
     * correctly even though they contain inner commas inside function calls.
     */
    data class ParametricExpression(override val formula: String) : Expression {
        val xFormula: String
        val yFormula: String

        init {
            // Strip  "r(t)="  prefix, then remove outer "(" … ")"
            val rhs = formula
                .substringAfter("=").trim()
                .removeSurrounding("(", ")")
                .trim()

            // Split at the first top-level comma (depth == 0)
            var depth = 0
            var splitIdx = -1
            for (i in rhs.indices) {
                when (rhs[i]) {
                    '(' -> depth++
                    ')' -> depth--
                    ',' -> if (depth == 0) { splitIdx = i; break }
                }
            }
            xFormula = if (splitIdx >= 0) rhs.substring(0, splitIdx).trim() else rhs
            yFormula = if (splitIdx >= 0) rhs.substring(splitIdx + 1).trim() else ""
        }
    }

    data class PointExpression(override val formula: String) : Expression {
        private val inner = formula.trim().removeSurrounding("(", ")")
        private val parts = inner.split(",")
        val x: Float = parts.getOrNull(0)?.trim()?.toFloatOrNull() ?: 0f
        val y: Float = parts.getOrNull(1)?.trim()?.toFloatOrNull() ?: 0f
    }

    /**
     * Integral shading.  Format:  integral(f(x), a, b)
     * The function string is split at the first top-level comma to avoid
     * splitting on commas inside nested function calls.
     */
    data class IntegralExpression(override val formula: String) : Expression {
        val function: String
        val lowerLimit: Float
        val upperLimit: Float

        init {
            val inner = formula
                .lowercase()
                .substringAfter("integral(")
                .removeSuffix(")")
                .trim()

            // Split on top-level commas
            val parts = splitTopLevel(inner)
            function    = parts.getOrNull(0)?.trim() ?: ""
            lowerLimit  = parts.getOrNull(1)?.trim()?.toFloatOrNull() ?: 0f
            upperLimit  = parts.getOrNull(2)?.trim()?.toFloatOrNull() ?: 0f
        }
    }

    data class AreaExpression(override val formula: String) : Expression

    data class VectorExpression(override val formula: String) : Expression {
        private val inner = formula.removePrefix("vec").trim().removeSurrounding("(", ")")
        private val parts = inner.split(",")
        val x: Float = parts.getOrNull(0)?.trim()?.toFloatOrNull() ?: 0f
        val y: Float = parts.getOrNull(1)?.trim()?.toFloatOrNull() ?: 0f
    }

    /** The raw arc formula; the actual numbers are parsed lazily by parseArc(). */
    data class ArcExpression(override val formula: String) : Expression
}

// ---------------------------------------------------------------------------
// Internal helper
// ---------------------------------------------------------------------------

private fun splitTopLevel(s: String): List<String> {
    val result = mutableListOf<String>()
    var depth = 0
    val current = StringBuilder()
    for (ch in s) {
        when {
            ch == '(' -> { depth++; current.append(ch) }
            ch == ')' -> { depth--; current.append(ch) }
            ch == ',' && depth == 0 -> { result.add(current.toString()); current.clear() }
            else -> current.append(ch)
        }
    }
    if (current.isNotEmpty()) result.add(current.toString())
    return result
}
