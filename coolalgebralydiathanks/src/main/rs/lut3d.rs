#pragma version(1)
#pragma rs java_package_name(cat.the.lydia.coolalgebralydiathanks.rs)
#pragma rs_fp_relaxed

//
// Created by lydia-schiff on 6/14/20
//
// RenderScript kernel code for ScriptC_lut3d class. Equivalent to ScriptIntrinsic3DLUT except the
// transform is applied at 32-bit float precision instead of clamping both LUT data and image data
// to 8-bit RGB.
//
// This allows us to apply ColorCubes to ColorCubes without losing any precision. We may do this
// repeatedly when doing ColorCube-concatination to implement funcion concatination for color func.
//
// The point of the CoolAlgebra is that ColorCube-concatination is an asscociative binary operation,
// so we need this level of precision to be able to call the operation associative with a straight
// face.
//

// This is currently implemented at fixed 17^3 size.
static const int N = 17; // ColorCube.N
static const int3 g_dims = { N, N, N };
static const float3 g_subdims = { N-1.0f, N-1.0f, N-1.0f };

rs_allocation g_lut_alloc;

// The origin of a local lattice cube and the offsets from the origin.
typedef struct Local_Position {
     int3 origin;
     float3 offset;
} Local_Position_t;

//
// "turn an input color into the 8 lattice points and a 3 scale factors we need"-step for
// tri-linear interpolation. We only need the input color and the dimensions of the cube to
// calculate this information.
//
// The result is used to sample colors from the correct locations in the 3D LUT and properly
// interpolate the result color. We are sort of "zooming in" to the local 2x2x2 lattice.
//
// We translate a color into a point in RGB space represented by the unit-cube. A Local_Position_t
// is defined by the origin, which determines the locations of the nearest color samples in 3D,
// and the offset, which is again a normalized float3 showing position in the local cube relative to
// the origin.
static Local_Position_t get_local_position(float3 color)
{
    float3 realPositionInIndexSpace = color * g_subdims;
    float3 intPositionInIndexSpace = floor(realPositionInIndexSpace);

    // 1 if position was N-1 and we mapped all the way to the end of the space, else 0f
    float3 edge = step(g_subdims, intPositionInIndexSpace);

    int3 origin = convert_int3(intPositionInIndexSpace - edge);
    float3 offset = (realPositionInIndexSpace - intPositionInIndexSpace) + edge;

    return (Local_Position_t) {origin, offset};
}

static float3 sample(int3 coord) {
    return rsGetElementAt_float3(g_lut_alloc, coord.x, coord.y, coord.z);
}

// Use trilinear interpolation to map an input color to an output color using a 3D LUT.
// I expect most of the work here is the 3 conditionals getting the Local_Position_t, and the 8
// texture fetches to get the local cube colors.
//
// I expect the 7 vectorized mix (linear-interpolation) calls are essentially free.
static float3 calculate_result_color(int3 origin, float3 offset) {

    // 8 nearest colors, the corners of our local cube.
    float3 c000 = sample(origin);
    float3 c001 = sample( origin + (int3){0, 0, 1});
    float3 c010 = sample(origin + (int3){0, 1, 0});
    float3 c011 = sample( origin + (int3){0, 1, 1});
    float3 c100 = sample( origin + (int3){1, 0, 0});
    float3 c101 = sample( origin + (int3){1, 0, 1});
    float3 c110 = sample( origin + (int3){1, 1, 0});
    float3 c111 = sample(origin + (int3){1, 1, 1});

    // Interpolate four times to collapse the blue channel of the local cube, which yields
    // a red-green-plane.
    float3 c00x = mix(c000, c001, offset.b);
    float3 c01x = mix(c010, c011, offset.b);
    float3 c10x = mix(c100, c101, offset.b);
    float3 c11x = mix(c110, c111, offset.b);

    // Interpolate twice more in the green direction to collapse the plane down to a line
    // parallel to the red axis.
    float3 c0xx = mix(c00x, c01x, offset.g);
    float3 c1xx = mix(c10x, c11x, offset.g);

    // Interpolate once more in the red direction to collapse the line to a point
    float3 result = mix(c0xx, c1xx,offset.r);

    return result;
}

static float3 apply_lut_3d(float3 color)
{
    Local_Position_t local_position = get_local_position(color);
    return calculate_result_color(local_position.origin, local_position.offset);
}

// rs kernel for applying 3D LUT to an Allocation of Element.F32_3
float3 RS_KERNEL apply_float3(float3 in)
{
    return apply_lut_3d(in);
}

// rs kernel for applying 3D LUT to an Allocation of Element.RGBA_8888
uchar4 RS_KERNEL apply_rgba_8888(uchar4 in)
{
    float3 color = rsUnpackColor8888(in).rgb;
    float3 result = apply_lut_3d(color);
    return rsPackColorTo8888((float4){result.r, result.g, result.b, 1.0f});
}
