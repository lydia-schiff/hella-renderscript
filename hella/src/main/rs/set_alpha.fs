#pragma version(1)
#pragma rs java_package_name(com.lydiaschiff.hella.renderer)

uchar alpha_value; // in  [0, 255]

uchar4 RS_KERNEL filter(uchar4 in)
{
    uchar4 out = in;
    out.a = alpha_value;
    return out;
}