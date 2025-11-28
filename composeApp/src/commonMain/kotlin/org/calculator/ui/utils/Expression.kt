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

class Expression(
    val formula: String,
    val type: ExpressionType
) {}