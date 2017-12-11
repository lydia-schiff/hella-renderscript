# Hella-Parallel Compute with RenderScript
- Example app for RGB Rendering for Bitmaps and the camera viewfinder.
- Associated talk: [_Hella-Parallel Compute with RenderScript_](https://youtu.be/eebG8SMYD20) at droidcon sf 2017.

## References!
### Essentials + source code (somtimes the best docs we have)
[RS Developer Guide](https://developer.android.com/guide/topics/renderscript/compute.html)

[RS Runtime API](https://developer.android.com/guide/topics/renderscript/reference/overview.html)

[RS Java api - Tests](https://android.googlesource.com/platform/frameworks/rs/+/master/tests/java_api)
- Good examples! In particular the ImageProcessing2 module.

[RS Java API source code](https://android.googlesource.com/platform/frameworks/base/+/master/rs/java/android/renderscript)

### Excellent examples with walkthoughs
#### Marchetti book + examples
[Renderscript: Parallel Computing on Android the Easy Way](https://hydex11.net/renderscript_parallel_computing_on_android_the_easy_way)
- [book examples](https://bitbucket.org/cmaster11/rsbookexamples)

[Google codelabs: Artistic style transfer and other advanced image editing](https://codelabs.developers.google.com/codelabs/android-style-transfer/index.html)
- neural network compute on device!

[Intel: Multimedia API Playback with RenderScript Effect](https://software.intel.com/en-us/articles/google-android-multimedia-api-playback-with-renderscript-effect)
- shows usage for MediaCodec and ImageFormat.NV21 YUV format

### Digging deeper
[RS runtime source code (C++)](https://android.googlesource.com/platform/frameworks/rs/+/master)

[RS driver reference implementations (C++)](https://android.googlesource.com/platform/frameworks/rs/+/master/cpu_ref/)

[RS scripting language offline compiler (slang)](https://android.googlesource.com/platform/frameworks/compile/slang/)

[RS scripting language back-end (jit, on device) compiler (bcc)](https://android.googlesource.com/platform/frameworks/compile/libbcc/+/master)

### Good general references
https://source.android.com/devices/graphics/architecture

https://developer.android.com/training/articles/perf-jni.html

https://developer.android.com/training/articles/smp.html

### Other options for compute (mostly GLES)
https://github.com/CyberAgent/android-gpuimage

https://github.com/googlesamples/android-MediaEffects

https://github.com/INDExOS/media-for-mobile

https://developer.android.com/guide/topics/graphics/2d-graphics.html

### project sources:
https://github.com/googlesamples/android-HdrViewfinder

https://github.com/googlesamples/android-BasicRenderScript

https://github.com/google/grafika
