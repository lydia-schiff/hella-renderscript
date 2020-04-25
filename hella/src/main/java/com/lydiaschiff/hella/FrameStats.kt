package com.lydiaschiff.hella

/**
 * Fps and dropped frame logger interface.
 */
interface FrameStats {
    fun logFrame(tag: String, nDropped: Int, totalDropped: Int, total: Int)
    fun clear()
}