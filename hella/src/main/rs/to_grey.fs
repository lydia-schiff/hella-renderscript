#pragma version(1)
#pragma rs java_package_name(com.lydiaschiff.hella.renderer)



float3 to_grey = (float3){0.3f, 0.6f, 0.1f};

// kernel function that converts each pixel to greyscale.
// Note that we don't have x-y coordinates in the function declaration because this is a pixel
// edit and we don't care.
uchar4 RS_KERNEL filter(uchar4 in)
{
    // We want to do the calculations in floats, so unpack the 32-bit uchar4 value into a vector of
    // 32-bit floats. There is a built in function for this.
    float4 in_floats = rsUnpackColor8888(in);

    // the built in dot-product function is overloaded for the float3 type
    float value = dot(in_floats.rgb, to_grey);

    // constructing the vec
    float4 out_floats = (float4) {value, value, value, 1.0f};

    return rsPackColorTo8888(out_floats);
}
