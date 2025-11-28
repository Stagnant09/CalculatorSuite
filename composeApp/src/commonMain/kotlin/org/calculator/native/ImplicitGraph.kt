package org.calculator.native

object NativePlot {
    init {
        loadLibrary("implicit_graph")
    }

    external fun computeImplicit(
        width: Int,
        height: Int,
        originX: Float,
        originY: Float,
        step: Float,
        scale: Float,
        threshold: Float,
        formula: String
    ): IntArray
}
