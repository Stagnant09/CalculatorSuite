package org.calculator.ui.utils

/*enum class ExpressionType {
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
}*/

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

    }

    data class PointExpression(
        override val formula: String
    ): Expression

    data class IntegralExpression(
        override val formula: String
    ): Expression

    data class AreaExpression(
        override val formula: String
    ): Expression

    data class VectorExpression(
        override val formula: String
    ): Expression

    data class ArcExpression(
        override val formula: String
    ): Expression
}