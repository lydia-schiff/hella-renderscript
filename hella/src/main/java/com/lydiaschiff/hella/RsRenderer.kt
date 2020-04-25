package com.lydiaschiff.hella

import android.renderscript.Allocation
import android.renderscript.RenderScript

/**
 * Renders edits from an input allocation to an output allocation. Both should have the same 2D
 * [android.renderscript.Type] with [android.renderscript.Element] RGBA_8888. An
 * RsRenderer instance should work for both Bitmap and RGB camera frame rendering.
 */
interface RsRenderer {
    /**
     * Render an edit to an input Allocation and write it to an output allocation. This must
     * always overwrite the out Allocation. This is called once for a Bitmap, and once per frame
     * for stream rendering.
     *
     * @param rs  RenderScript context
     * @param in  input RGBA_8888 allocation
     * @param out output RGBA_8888 allocation
     */
    fun renderFrame(rs: RenderScript, inAlloc: Allocation, outAlloc: Allocation)

    /**
     * Short description of the renderer.
     */
    val name: String

    /**
     * Check if this is a pixel edit where the in and out allocations can be the same object.
     * This allows for optimizations in certain cases.
     */
    val canRenderInPlace: Boolean
}