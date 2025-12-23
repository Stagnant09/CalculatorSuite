package org.calculator.ui.utils

enum class ExpressionType {
    CARTESIAN_Y_X, // y = f(x)
    CARTESIAN_X, // x = c, c is a constant
    CARTESIAN_IMPLICIT, // f(x,y) = 0, a*x^n + c = 0 etc.
    POLAR_R_U, // r = f(u)
    POLAR_U, // u = c
    PARAMETRIC, // r(t) = (x(t), y(t))
    POINT, // (x,y)
    INTEGRAL, // Integral(f(x), a, b)
    AREA, // [(x1, y1), (x2,y2), ...]
    VECTOR, // vec(x,y)
    ARC // arc((x,y), r, u)
}

data class ArcExpression(
    val x: Float,
    val y: Float,
    val r: Float,
    val start: Float,
    val sweep: Float
)

data class Vector(
    val x: Float,
    val y: Float
)

sealed interface Expression {
    val formula: String
    data class CartesianYXExpression(
        override val formula: String
    ): Expression

    data class CartesianXExpression(
        override val formula: String
    ): Expression {
        val constant: Float = formula.split("=").last().trim().toFloat()
    }

    data class CartesianImplicitExpression(
        override val formula: String
    ): Expression

    data class PolarRUExpression(
        override val formula: String
    ): Expression

    data class PolarUExpression(
        override val formula: String
    ): Expression {
        val constant: Float = formula.split("=").last().trim().toFloat()
    }

    data class ParametricExpression(
        override val formula: String
    ): Expression {
        // formula is written in the form: r(t) = (x(t), y(t))
        val xFormula: String = formula.split("=").last().removePrefix("(").removeSuffix(")").split(",").first().trim()
        val yFormula: String = formula.split("=").last().removePrefix("(").removeSuffix(")").split(",").last().trim()
    }

    data class PointExpression(
        override val formula: String
    ): Expression {
        val x: Float = formula.removePrefix("(").removeSuffix(")").split(",").first().trim().toFloat()
        val y: Float = formula.removePrefix("(").removeSuffix(")").split(",").last().trim().toFloat()
    }

    data class IntegralExpression(
        override val formula: String
    ): Expression { // TODO: Fix
        val function: String = formula.removePrefix("Integral").removePrefix("(").removeSuffix(")").split(",").first().trim()
        val lowerLimit: Float = formula.removePrefix("Integral").removePrefix("(").removeSuffix(")").split(",").last().trim().toFloat()
        val upperLimit: Float = formula.removePrefix("Integral").removePrefix("(").removeSuffix(")").split(",").last().trim().toFloat()
    }

    data class AreaExpression(
        override val formula: String
    ): Expression

    data class VectorExpression(
        override val formula: String
    ): Expression {
        val x: Float = formula.removePrefix("vec").removePrefix("(").removeSuffix(")").split(",").first().trim().toFloat()
        val y: Float = formula.removePrefix("vec").removePrefix("(").removeSuffix(")").split(",").last().trim().toFloat()
    }

    data class ArcExpression(
        override val formula: String
    ): Expression {
        val center: PointExpression = PointExpression(formula.removePrefix("arc").removePrefix("(").removeSuffix(")").split(",").first().trim())
        val radius: Float = formula.removePrefix("arc").removePrefix("(").removeSuffix(")").split(",").last().trim().toFloat()
        val angle: Float = formula.removePrefix("arc").removePrefix("(").removeSuffix(")").split(",").last().trim().toFloat()
    }
}