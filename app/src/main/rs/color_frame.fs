#pragma version(1)
#pragma rs java_package_name(com.lydiaschiff.hellaparallel.renderers)

static rs_allocation in;
static uint half_width, half_height;

// ScriptC_fill_color.set_color(Short4)
uchar4 color;

static uchar4 read_pixel_from_scaled_alloc(uint x, uint y)
{
    uint xScaled = x - half_width;
    uint yScaled = y - half_height;
    return rsGetElementAt_uchar4(in, xScaled, yScaled);
}

static bool in_color_frame(uint x, uint y)
{
    return (x <= half_width  || x >= 3 * half_width || y <= half_height|| y >= 3 * half_height);
}

uchar4 RS_KERNEL frame_image(uchar4 unused_in, uint32_t x, uint32_t y)
{
    if (in_color_frame(x, y))
        return color;

    return read_pixel_from_scaled_alloc(x, y);
}

void prepare(rs_allocation in_alloc)
{
    in = in_alloc;
    half_width = rsAllocationGetDimX(in) / 2;
    half_height = rsAllocationGetDimY(in) / 2;
}
