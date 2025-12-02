package org.calculator.nativeLib

expect class ImplicitPlotter() {
    fun setFormula(formula: String)
    fun setFormula2(formula: String)
    // returns flat IntArray width*height with 0/1
    fun evaluateBitmap(
        width: Int,
        height: Int,
        originX: Float,
        originY: Float,
        step: Float,
        scale: Float,
        threshold: Float
    ): IntArray

    fun meetPoints(
        width: Int,
        height: Int,
        originX: Float,
        originY: Float,
        step: Float,
        scale: Float,
        threshold: Float
    ): IntArray
}
