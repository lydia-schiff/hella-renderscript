package com.lydiaschiff.hella

import android.graphics.ImageFormat
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.Type
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt

/**
 * Static utilities for Renderscript. I'm going to leave in any unneeded API annotations because
 * they are interesting documentation.
 */
object RsUtil {

    fun width(a: Allocation): Int = a.type.x
    fun height(a: Allocation): Int = a.type.y

    /**
     * Create a sized RGBA_8888 Allocation to use with scripts.
     *
     * @param rs RenderScript context
     * @param x  width in pixels
     * @param y  height in pixels
     * @return an RGBA_8888 Allocation
     */
    fun createRgbAlloc(rs: RenderScript, x: Int, y: Int) =
            Allocation.createTyped(rs, createType(rs, Element.RGBA_8888(rs), x, y))

    /**
     * Create an RGBA_8888 allocation that can act as a Surface producer. This lets us call
     * [Allocation.setSurface] and call [Allocation.ioSend]. If
     * you wanted to read the data from this Allocation, do so before calling ioSend(), because
     * after, the data is undefined.
     *
     * @param rs rs context
     * @param x  width in pixels
     * @param y  height in pixels
     * @return an RGBA_8888 Allocation with USAGE_IO_INPUT
     */
    @RequiresApi(16)
    fun createRgbIoOutputAlloc(rs: RenderScript, x: Int, y: Int) =
            Allocation.createTyped(rs, createType(rs, Element.RGBA_8888(rs), x, y),
                    Allocation.USAGE_IO_OUTPUT or Allocation.USAGE_SCRIPT)

    /**
     * Create an Allocation with the same Type (size and elements) as a source Allocation.
     *
     * @param rs rs context
     * @param a  source alloc
     * @return a new Allocation with matching Type
     */
    fun createMatchingAlloc(rs: RenderScript, a: Allocation) = Allocation.createTyped(rs, a.type)

    fun createMatchingAllocScaled(rs: RenderScript, a: Allocation,
                                  scaled: Float): Allocation {
        val scaledX = scaled.toInt() * width(a)
        val scaledY = scaled.toInt() * height(a)
        return Allocation.createTyped(rs, createType(rs, a.type.element, scaledX, scaledY))
    }

    /**
     * Create an YUV allocation that can act as a Surface consumer. This lets us call
     * [Allocation.getSurface], set a [Allocation.OnBufferAvailableListener]
     * callback to be notified when a frame is ready, and call [Allocation.ioReceive] to
     * latch a frame and access its yuv pixel data.
     *
     * The yuvFormat should be {@value ImageFormat#YUV_420_888}, {@value ImageFormat#NV21}, or maybe
     * [ImageFormat.YV12].
     *
     * @param rs        RenderScript context
     * @param x         width in pixels
     * @param y         height in pixels
     * @param yuvFormat yuv pixel format
     * @return a YUV Allocation with USAGE_IO_INPUT
     */
    @RequiresApi(18)
    fun createYuvIoInputAlloc(rs: RenderScript, x: Int, y: Int, yuvFormat: Int) =
            Allocation.createTyped(rs, createYuvType(rs, x, y, yuvFormat),
                    Allocation.USAGE_IO_INPUT or Allocation.USAGE_SCRIPT)

    fun createType(rs: RenderScript, e: Element, x: Int, y: Int): Type =
            if (Build.VERSION.SDK_INT >= 21) Type.createXY(rs, e, x, y)
            else Type.Builder(rs, e).setX(x).setY(y).create()

    @RequiresApi(18)
    fun createYuvType(rs: RenderScript, x: Int, y: Int, yuvFormat: Int): Type {
        var supported = yuvFormat == ImageFormat.NV21 || yuvFormat == ImageFormat.YV12
        if (Build.VERSION.SDK_INT >= 19) {
            supported = supported or (yuvFormat == ImageFormat.YUV_420_888)
        }
        require(supported) { "invalid yuv format: $yuvFormat" }
        return Type.Builder(rs, createYuvElement(rs)).setX(x).setY(y).setYuvFormat(yuvFormat)
                .create()
    }

    fun createYuvElement(rs: RenderScript?): Element =
            if (Build.VERSION.SDK_INT >= 19) Element.YUV(rs)
            else Element.createPixel(rs, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_YUV)

    /**
     * A 3D Type suitable for color LUT data for use with ScriptIntrinsic3DLUT.
     */
    fun lut3dType(rs: RenderScript, x: Int, y: Int = x, z: Int = x) =
            if (Build.VERSION.SDK_INT >= 21) Type.createXYZ(rs, Element.RGBA_8888(rs), x, y, x)
            else Type.Builder(rs, Element.RGBA_8888(rs)).setX(x).setY(y).setZ(z).create()

    /**
     * A cubic 3D Allocation suitable for color LUT data for use with ScriptIntrinsic3DLUT.
     */
    fun lut3dAlloc(
            rs: RenderScript,
            setIdentity: Boolean,
            x: Int,
            y: Int = x,
            z: Int = x
    ): Allocation = Allocation.createTyped(rs, lut3dType(rs, x, y, z)).also {
        if (setIdentity) IdentityLut.setIdentityLutData(it)
    }


    fun copyColorIntsToAlloc(colorInts: IntArray, lut3dAlloc: Allocation) {
        val buffer = PooledBuffer.acquire(colorInts.size)
        colorInts.forEachIndexed { n, c -> buffer[n] = colorIntToRsPackedColor8888(c) }
        lut3dAlloc.copyFromUnchecked(buffer)
        PooledBuffer.returnBuffer(buffer)
    }

    fun copyRgbFloatsToAlloc(rgbFloats: FloatArray, lut3dAlloc: Allocation) {
        val nColors = rgbFloats.size / 3
        require(lut3dAlloc.type.count == nColors)
        val buffer = PooledBuffer.acquire(nColors)
        for (n in 0 until nColors) {
            buffer[n] = rsPackedColor8888(rgbFloats, n * 3)
        }
        lut3dAlloc.copyFromUnchecked(buffer)
        PooledBuffer.returnBuffer(buffer)
    }

    /**
     * Able to be copied unchecked into an alloc with RGBA_8888 or U8_4 elements.
     */
    fun rsPackedColor8888(r: Int, g: Int, b: Int): Int = 0xff00000 or (b shl 16) or (g shl 8) or r
    fun rsPackedColor8888(r: Float, g: Float, b: Float): Int = rsPackedColor8888(
            (r * 255).roundToInt() and 0xff,
            (g * 255).roundToInt() and 0xff,
            (b * 255).roundToInt() and 0xff)

    fun rsPackedColor8888(rgbFloats: FloatArray, start: Int) = rsPackedColor8888(
            rgbFloats[start],
            rgbFloats[start + 1],
            rgbFloats[start + 2])

    fun colorIntToRsPackedColor8888(@ColorInt c: Int): Int = swapRb(c)

    @ColorInt
    fun rsPackedColor8888ToColorInt(c: Int): Int = swapRb(c)

    /**
     * When we convert an Android ColorInt unchecked into renderscript U8_4, we have to swap red
     * and blue. Much endian. Very bit-shifting.
     */
    private fun swapRb(c: Int): Int {
        val r = c shr 16 and 0xff
        val g = c shr 8 and 0xff
        val b = c and 0xff
        return 0xff00000 or (b shl 16) or (g shl 8) or r
    }

    /**
     * There are certain calling patterns during rendering where we may need to update a 3D LUT
     * allocation using an IntArray of fixed size as often as several times per-second. This is a
     * thread-safe way to not allocate big IntArrays repeatedly inside a render loop.
     */
    object PooledBuffer {
        private val pooledBuffer = AtomicReference<IntArray>()

        fun acquire(size: Int): IntArray = pooledBuffer.getAndSet(null)
                ?.takeIf { it.size == size } ?: IntArray(size)

        fun returnBuffer(buffer: IntArray) = pooledBuffer.compareAndSet(null, buffer)

        fun release() = pooledBuffer.set(null)
    }
}