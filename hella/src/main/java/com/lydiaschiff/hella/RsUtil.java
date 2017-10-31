package com.lydiaschiff.hella;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.support.annotation.RequiresApi;
import android.view.Surface;

/**
 * Static utilities for Renderscript.
 */
@SuppressLint("ObsoleteSdkInt")
public final class RsUtil {

    private RsUtil() {} // no instances

    public static int width(Allocation a) {
        return a.getType().getX();
    }

    public static int height(Allocation a) {
        return a.getType().getY();
    }

    /**
     * Create a sized RGBA_8888 Allocation to use with scripts.
     *
     * @param rs RenderScript context
     * @param x  width in pixels
     * @param y  height in pixels
     * @return an RGBA_8888 Allocation
     */
    public static Allocation createRgbAlloc(RenderScript rs, int x, int y) {
        return Allocation.createTyped(rs, createType(rs, Element.RGBA_8888(rs), x, y));
    }

    /**
     * Create an RGBA_8888 allocation that can act as a Surface producer. This lets us call
     * {@link Allocation#setSurface(Surface)} and call {@link Allocation#ioSend()}. If
     * you wanted to read the data from this Allocation, do so before calling ioSend(), because
     * after, the data is undefined.
     *
     * @param rs rs context
     * @param x  width in pixels
     * @param y  height in pixels
     * @return an RGBA_8888 Allocation with USAGE_IO_INPUT
     */
    @RequiresApi(16)
    public static Allocation createRgbIoOutputAlloc(RenderScript rs, int x, int y) {
        return Allocation.createTyped(rs, createType(rs, Element.RGBA_8888(rs), x, y),
                Allocation.USAGE_IO_OUTPUT | Allocation.USAGE_SCRIPT);
    }

    /**
     * Create an Allocation with the same Type (size and elements) as a source Allocation.
     *
     * @param rs rs context
     * @param a  source alloc
     * @return a new Allocation with matching Type
     */
    public static Allocation createMatchingAlloc(RenderScript rs, Allocation a) {
        return Allocation.createTyped(rs, a.getType());
    }

    public static Allocation createMatchingAllocScaled(RenderScript rs, Allocation a,
            float scaled) {
        int scaledX = (int) scaled * width(a);
        int scaledY = (int) scaled * height(a);
        return Allocation.createTyped(rs, createType(rs, a.getType().getElement(), scaledX, scaledY));
    }

    /**
     * Create an YUV allocation that can act as a Surface consumer. This lets us call
     * {@link Allocation#getSurface()}, set a {@link Allocation.OnBufferAvailableListener}
     * callback to be notified when a frame is ready, and call {@link Allocation#ioReceive()} to
     * latch a frame and access its yuv pixel data.
     * <p>
     * The yuvFormat should be {@value ImageFormat#YUV_420_888}, {@value ImageFormat#NV21}, or maybe
     * {@link ImageFormat#YV12}.
     *
     * @param rs        RenderScript context
     * @param x         width in pixels
     * @param y         height in pixels
     * @param yuvFormat yuv pixel format
     * @return a YUV Allocation with USAGE_IO_INPUT
     */
    @RequiresApi(18)
    public static Allocation createYuvIoInputAlloc(RenderScript rs, int x, int y, int yuvFormat) {
        return Allocation.createTyped(rs, createYuvType(rs, x, y, yuvFormat),
                Allocation.USAGE_IO_INPUT | Allocation.USAGE_SCRIPT);
    }

    public static Type createType(RenderScript rs, Element e, int x, int y) {
        if (Build.VERSION.SDK_INT >= 21) {
            return Type.createXY(rs, e, x, y);
        } else {
            return new Type.Builder(rs, e).setX(x).setY(y).create();
        }
    }

    @RequiresApi(18)
    public static Type createYuvType(RenderScript rs, int x, int y, int yuvFormat) {
        boolean supported = yuvFormat == ImageFormat.NV21 || yuvFormat == ImageFormat.YV12;
        if (Build.VERSION.SDK_INT >= 19) {
            supported |= yuvFormat == ImageFormat.YUV_420_888;
        }
        if (!supported) {
            throw new IllegalArgumentException("invalid yuv format: " + yuvFormat);
        }
        return new Type.Builder(rs, createYuvElement(rs)).setX(x).setY(y).setYuvFormat(yuvFormat)
                .create();
    }

    public static Element createYuvElement(RenderScript rs) {
        if (Build.VERSION.SDK_INT >= 19) {
            return Element.YUV(rs);
        } else {
            return Element.createPixel(rs, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_YUV);
        }
    }
}
