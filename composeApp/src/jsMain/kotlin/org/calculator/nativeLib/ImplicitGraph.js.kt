package org.calculator.nativeLib

actual class ImplicitPlotter actual constructor() {
    actual fun setFormula(formula: String) {
    }

    actual fun evaluateBitmap(
        width: Int,
        height: Int,
        originX: Float,
        originY: Float,
        step: Float,
        scale: Float,
        threshold: Float
    ): IntArray {
        TODO("Not yet implemented")
    }
}