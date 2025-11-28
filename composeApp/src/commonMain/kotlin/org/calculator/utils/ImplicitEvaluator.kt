package org.calculator.utils

import org.calculator.ui.utils.Expression

class ImplicitEvaluator(
    private val expression: Expression
) {
    fun evaluate(x: Double, y: Double): Float {
        return try {
            expression.evaluate(Pair(x,y))
        } catch (e: Exception) {
            Float.NaN
        }
    }
}
