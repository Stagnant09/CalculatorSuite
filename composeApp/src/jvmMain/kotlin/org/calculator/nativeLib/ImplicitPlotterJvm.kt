package org.calculator.nativeLib

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer

private interface CLib : com.sun.jna.Library {
    fun ig_set_formula(formula: String?)
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

        val result = IntArray(size)
        var offset = 0L
        for (i in 0 until size) {
            // read 32-bit signed int little-endian
            result[i] = mem.getInt(offset)
            offset += 4
        }
        return result
    }
}
