package cat.the.lydia.coolalgebralydiathanks.utils

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import cat.the.lydia.coolalgebralydiathanks.Bounded
import cat.the.lydia.coolalgebralydiathanks.Color
import cat.the.lydia.coolalgebralydiathanks.ColorCube
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.collections.ArrayList

/**
 * Code borrowed with love from:
 * https://github.com/Vensent/RsLutDemo/blob/master/app/src/main/java/com/vensent/lut/LoadCubeFileActivity.java
 * todo: This is finicky and only works on luts pre-converted to 17^3.
 */
object CubeFileParser {

    fun loadCubeResource(c: Context, @RawRes id: Int): ColorCube {
        val inputStream = c.resources.openRawResource(id)
        return parseCubeFile(inputStream)
    }

    private fun parseCubeFile(inputStream: InputStream): ColorCube {
        var data = ArrayList<Color>(ColorCube.N_COLORS)
        var lut3dSize = 0
        var reader: BufferedReader? = null
        var i = 0
        try {
            reader = BufferedReader(InputStreamReader(inputStream))
            // do reading, usually loop until end of file reading
            lateinit var line: String
            var parts: Array<String>
            while (reader.readLine().also({ line = it }) != null) {
                if (line.startsWith("#") || line.isEmpty()) {
                    continue
                }
                parts = line.toLowerCase().split(" ").toTypedArray()
                if (parts.isEmpty()) {
                    continue
                }
                if (parts[0] == "title" || line.toLowerCase().startsWith("title") || line.toLowerCase().startsWith("lut_3d_size")) {
                    // optional, or do nothing.
                } else if (parts[0] == "lut_1d_size" || parts[0] == "lut_2d_size") {
                    throw Exception("Unsupported Iridas .cube lut tag: " + parts[0])
                } else if (parts[0] == "lut_3d_size") {
                    if (parts.size != 2) {
                        throw Exception("Malformed LUT_3D_SIZE tag in Iridas .cube lut.")
                    }
                    lut3dSize = parts[1].toInt()
                    data = ArrayList<Color>(lut3dSize * lut3dSize * lut3dSize)
                } else if (parts[0] == "domain_min") {
                    if (parts.size != 4 || parts[1].toFloat() != 0.0f || parts[2].toFloat() != 0.0f || parts[3].toFloat() != 0.0f) {
                        throw Exception("domain_min is not correct.")
                    }
                } else if (parts[0] == "domain_max") {
                    if (parts.size != 4 || parts[1].toFloat() != 1.0f || parts[2].toFloat() != 1.0f || parts[3].toFloat() != 1.0f) {
                        throw Exception("domain_max is not correct.")
                    }
                } else {
                    // It must be a float triple!
                    if (data == null || data.isEmpty()) {
  //                      continue
                      //  throw Exception("The file doesn't contain 'lut_3d_size'.")
                    }

                    // In a .cube file, each data line contains 3 floats.
                    // Please note: the blue component goes first!!!
                    data.add( getRGBColorValue(parts[0].toFloat(), parts[1].toFloat(), parts[2].toFloat()))
                }
            }
        } catch (e: IOException) {
            //log the exception
        } catch (e: NumberFormatException) {
            Log.d(TAG, "Converting string to digit failed.")
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    //log the exception
                }
            }
        }
        return ColorCube(data)
    }

    private fun getRGBColorValue(r: Float, g: Float, b: Float): Color {
        return Color(Bounded(r), Bounded(g), Bounded(b))
    }

    private const val TAG = "CubeFileParser"
}