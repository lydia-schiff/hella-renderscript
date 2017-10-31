#pragma version(1)
#pragma rs java_package_name(com.lydiaschiff.hellaparallel.renderers)

uchar alpha;

uchar4 RS_KERNEL filter(uchar4 in)
{
    uchar4 out = in;
    out.a = alpha;
    return out;
}