package ai.arcblroth.boss.util

import org.joml.Vector4d
import org.joml.Vector4f
import kotlin.math.floor

/**
 * A standalone RGBA color class, based on
 * [the original](https://github.com/Arc-blroth/BosstroveRevenge/blob/try1/core/src/main/java/ai/arcblroth/boss/render/Color.java)
 * in Java.
 *
 * The RGB &harr; HSV conversion functions have been rewritten based on the formulas in
 * [this document](https://mattlockyer.github.io/iat455/documents/rgb-hsv.pdf).
 *
 * @author Arc'blroth
 */
data class Color(val rgba: Int) {

    val red: Int
        get() = rgba shr 16 and 0xFF

    val green: Int
        get() = rgba shr 8 and 0xFF

    val blue: Int
        get() = rgba shr 0 and 0xFF

    val alpha: Int
        get() = rgba shr 24 and 0xFF

    @JvmOverloads
    constructor(r: Int, g: Int, b: Int, a: Int = 255) : this(
        (clipRGBA(a) and 0xFF shl 24) or
            (clipRGBA(r) and 0xFF shl 16) or
            (clipRGBA(g) and 0xFF shl 8) or
            (clipRGBA(b) and 0xFF shl 0)
    )

    override fun toString(): String {
        return "ai.arcblroth.boss.render.Color[r=$red, g=$green, b=$blue, a=$alpha]"
    }

    companion object {
        /**
         * Converts a color in the HSV color space to RGB.
         */
        @JvmStatic
        fun fromHSV(hsv: Vector4d): Color = fromHSV(hsv.x, hsv.y, hsv.z, hsv.w)

        /**
         * Converts a color in the HSV color space to RGB.
         */
        @JvmOverloads
        @JvmStatic
        @Suppress("NAME_SHADOWING")
        fun fromHSV(h: Double, s: Double, v: Double, a: Double = 1.0): Color {
            val h = h * 6.0 % 6
            val hFract = h - floor(h)

            val α = (v * (1.0 - s) * 255.0).toInt()
            val β = (v * (1 - hFract * s) * 255.0).toInt()
            val γ = (v * (1 - (1 - hFract) * s) * 255.0).toInt()
            val v = (v * 255.0).toInt()
            val a = (a * 255.0).toInt()

            return when {
                0 <= h && h < 1 -> Color(v, γ, α, a)
                1 <= h && h < 2 -> Color(β, v, α, a)
                2 <= h && h < 3 -> Color(α, v, γ, a)
                3 <= h && h < 4 -> Color(α, β, v, a)
                4 <= h && h < 5 -> Color(γ, α, v, a)
                5 <= h && h < 6 -> Color(v, α, β, a)
                else -> Color(v, v, v, a)
            }
        }
    }

    /**
     * Converts this color in the RGB color space to HSV.
     */
    fun toHSV(): Vector4d {
        val red = red.toDouble() / 255.0
        val green = green.toDouble() / 255.0
        val blue = blue.toDouble() / 255.0
        val alpha = alpha.toDouble() / 255.0

        val maxRGB = maxOf(red, green, blue)
        val minRGB = minOf(red, green, blue)
        val delta = maxRGB - minRGB

        val h = when {
            delta == 0.0 -> 0.0
            red == maxRGB -> (green - blue) / delta
            green == maxRGB -> (blue - red) / delta + 2.0
            blue == maxRGB -> (red - green) / delta + 4.0
            else -> throw RuntimeException("The colors are off the charts!")
        }
        val hPositive = (h % 6 + 6) % 6 / 6
        val s = if (maxRGB == 0.0) 0.0 else delta / maxRGB

        return Vector4d(hPositive, s, maxRGB, alpha)
    }
}

/**
 * Clips the [channelValue] to between 0 and 255.
 */
private fun clipRGBA(channelValue: Int) = channelValue.coerceIn(0, 255)

/**
 * Converts this color into a new [Vector4f] object
 * for usage with the rendering backend.
 */
fun Color.asVector4f() = this.asVector4f(Vector4f())

/**
 * Converts this color and stores the result in the
 * given Vector4f for usage with the rendering backend.
 */
fun Color.asVector4f(target: Vector4f): Vector4f {
    target.x = this.red / 255.0f
    target.y = this.green / 255.0f
    target.z = this.blue / 255.0f
    target.w = this.alpha / 255.0f
    return target
}

// These default color constants are taken from the java.awt.Color class.
val WHITE = Color(255, 255, 255)
val LIGHT_GRAY = Color(192, 192, 192)
val GRAY = Color(128, 128, 128)
val DARK_GRAY = Color(64, 64, 64)
val BLACK = Color(0, 0, 0)
val RED = Color(255, 0, 0)
val PINK = Color(255, 175, 175)
val ORANGE = Color(255, 200, 0)
val YELLOW = Color(255, 255, 0)
val GREEN = Color(0, 255, 0)
val MAGENTA = Color(255, 0, 255)
val CYAN = Color(0, 255, 255)
val BLUE = Color(0, 0, 255)
val TRANSPARENT = Color(0, 0, 0, 0)
