package org.calculator.nativeLib

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer

private interface CLib : com.sun.jna.Library {
    fun ig_set_formula(formula: String?)
    fun ig_set_formula2(formula: String?)
    fun ig_evaluate_bitmap(
        width: Int,
        height: Int,
        originX: Float,
        originY: Float,
        step: Float,
        scale: Float,
        threshold: Float,
        outBitmap: Pointer
    ): Int

    fun ig_meet_points_of_f1_f2(
        width: Int,
        height: Int,
        originX: Float,
        originY: Float,
        step: Float,
        scale: Float,
        threshold: Float,
        outBitmap: Pointer
    ): Int
}

actual class ImplicitPlotter {
    private val lib: CLib = Native.load("implicit_graph", CLib::class.java) as CLib

    /*init {
        // System.loadLibrary can be used if needed, but JNA will auto-load from library path:
        // On runtime, place native lib named implicit_graph(.so/.dylib/.dll) in java.library.path
        lib = Native.load("implicit_graph", CLib::class.java) as CLib
    }*/

    actual fun setFormula(formula: String) {
        lib.ig_set_formula(formula)
    }

    actual fun setFormula2(formula: String) {
        lib.ig_set_formula2(formula)
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
        val size = width * height
        // allocate int32 array
        val mem = Memory(size.toLong() * 4) // 4 bytes per int
        val ret = lib.ig_evaluate_bitmap(width, height, originX, originY, step, scale, threshold, mem)

        if (ret != 0) {
            return IntArray(size) { 0 }
        }

        return mem.getIntArray(0, size)
    }

    actual fun meetPoints(
        width: Int,
        height: Int,
        originX: Float,
        originY: Float,
        step: Float,
        scale: Float,
        threshold: Float
    ): IntArray {
        val size = width * height
        // allocate int32 array
        val mem = Memory(size.toLong() * 4) // 4 bytes per int
        val ret = lib.ig_meet_points_of_f1_f2(width, height, originX, originY, step, scale, threshold, mem)

        if (ret != 0) {
            return IntArray(size) { 0 }
        }

        return mem.getIntArray(0, size)
    }
}
