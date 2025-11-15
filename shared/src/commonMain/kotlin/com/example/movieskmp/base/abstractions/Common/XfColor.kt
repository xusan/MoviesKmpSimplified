package com.base.abstractions.Common

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class XfColor
{
    private val _mode: Mode

    private enum class Mode
    {
        Default,
        Rgb,
        Hsl
    }

    val IsDefault: Boolean
        get() = _mode == Mode.Default

    private val _a: Float

    val A: Double
        get() = _a.toDouble()

    private val _r: Float

    val R: Double
        get() = _r.toDouble()

    private val _g: Float

    val G: Double
        get() = _g.toDouble()

    private val _b: Float

    val B: Double
        get() = _b.toDouble()

    private val _hue: Float

    val Hue: Double
        get() = _hue.toDouble()

    private val _saturation: Float

    val Saturation: Double
        get() = _saturation.toDouble()

    private val _luminosity: Float

    val Luminosity: Double
        get() = _luminosity.toDouble()

    constructor(r: Double, g: Double, b: Double, a: Double) : this(r, g, b, a, Mode.Rgb)

    private constructor(w: Double, x: Double, y: Double, z: Double, mode: Mode)
    {
        _mode = mode
        when (mode)
        {
            Mode.Default ->
            {
                _r = -1f
                _g = -1f
                _b = -1f
                _a = -1f
                _hue = -1f
                _saturation = -1f
                _luminosity = -1f
            }
            Mode.Rgb ->
            {
                _r = Clamp(w, 0.0, 1.0).toFloat()
                _g = Clamp(x, 0.0, 1.0).toFloat()
                _b = Clamp(y, 0.0, 1.0).toFloat()
                _a = Clamp(z, 0.0, 1.0).toFloat()
                val hslResult = ConvertToHsl(_r, _g, _b, mode)
                _hue = hslResult.h
                _saturation = hslResult.s
                _luminosity = hslResult.l
            }
            Mode.Hsl ->
            {
                _hue = Clamp(w, 0.0, 1.0).toFloat()
                _saturation = Clamp(x, 0.0, 1.0).toFloat()
                _luminosity = Clamp(y, 0.0, 1.0).toFloat()
                _a = Clamp(z, 0.0, 1.0).toFloat()
                val rgbResult = ConvertToRgb(_hue, _saturation, _luminosity, mode)
                _r = rgbResult.r
                _g = rgbResult.g
                _b = rgbResult.b
            }
        }
    }

    private constructor(r: Int, g: Int, b: Int)
    {
        _mode = Mode.Rgb
        _r = r / 255f
        _g = g / 255f
        _b = b / 255f
        _a = 1f
        val hslResult = ConvertToHsl(_r, _g, _b, _mode)
        _hue = hslResult.h
        _saturation = hslResult.s
        _luminosity = hslResult.l
    }

    private constructor(r: Int, g: Int, b: Int, a: Int)
    {
        _mode = Mode.Rgb
        _r = r / 255f
        _g = g / 255f
        _b = b / 255f
        _a = a / 255f
        val hslResult = ConvertToHsl(_r, _g, _b, _mode)
        _hue = hslResult.h
        _saturation = hslResult.s
        _luminosity = hslResult.l
    }

    constructor(r: Double, g: Double, b: Double) : this(r, g, b, 1.0)

    constructor(value: Double) : this(value, value, value, 1.0)

    fun MultiplyAlpha(alpha: Double): XfColor
    {
        when (_mode)
        {
            Mode.Default -> throw IllegalStateException("Invalid on Color.Default")
            Mode.Rgb -> return XfColor(_r.toDouble(), _g.toDouble(), _b.toDouble(), _a * alpha, Mode.Rgb)
            Mode.Hsl -> return XfColor(_hue.toDouble(), _saturation.toDouble(), _luminosity.toDouble(), _a * alpha, Mode.Hsl)
        }
    }

    fun AddLuminosity(delta: Double): XfColor
    {
        if (_mode == Mode.Default)
            throw IllegalStateException("Invalid on Color.Default")

        return XfColor(_hue.toDouble(), _saturation.toDouble(), _luminosity + delta, _a.toDouble(), Mode.Hsl)
    }

    fun WithHue(hue: Double): XfColor
    {
        if (_mode == Mode.Default)
            throw IllegalStateException("Invalid on Color.Default")
        return XfColor(hue, _saturation.toDouble(), _luminosity.toDouble(), _a.toDouble(), Mode.Hsl)
    }

    fun WithSaturation(saturation: Double): XfColor
    {
        if (_mode == Mode.Default)
            throw IllegalStateException("Invalid on Color.Default")
        return XfColor(_hue.toDouble(), saturation, _luminosity.toDouble(), _a.toDouble(), Mode.Hsl)
    }

    fun WithLuminosity(luminosity: Double): XfColor
    {
        if (_mode == Mode.Default)
            throw IllegalStateException("Invalid on Color.Default")
        return XfColor(_hue.toDouble(), _saturation.toDouble(), luminosity, _a.toDouble(), Mode.Hsl)
    }

    override fun equals(other: Any?): Boolean
    {
        if (other is XfColor)
        {
            return EqualsInner(this, other)
        }
        return super.equals(other)
    }

    private fun EqualsInner(color1: XfColor, color2: XfColor): Boolean
    {
        if (color1._mode == Mode.Default && color2._mode == Mode.Default)
            return true
        if (color1._mode == Mode.Default || color2._mode == Mode.Default)
            return false
        if (color1._mode == Mode.Hsl && color2._mode == Mode.Hsl)
            return color1._hue == color2._hue && color1._saturation == color2._saturation && color1._luminosity == color2._luminosity && color1._a == color2._a
        return color1._r == color2._r && color1._g == color2._g && color1._b == color2._b && color1._a == color2._a
    }

    override fun hashCode(): Int
    {
        var hashcode = _r.hashCode()
        hashcode = (hashcode * 397) xor _g.hashCode()
        hashcode = (hashcode * 397) xor _b.hashCode()
        hashcode = (hashcode * 397) xor _a.hashCode()
        return hashcode
    }

    override fun toString(): String
    {
        return "[Color: A=$A, R=$R, G=$G, B=$B, Hue=$Hue, Saturation=$Saturation, Luminosity=$Luminosity]"
    }

    fun ToHex(): String
    {
        fun toHexByte(value: Double): String {
            val intVal = (value * 255).toInt().coerceIn(0, 255)
            val hex = intVal.toString(16).uppercase()
            return if (hex.length == 1) "0$hex" else hex
        }

        return "#${toHexByte(A)}${toHexByte(R)}${toHexByte(G)}${toHexByte(B)}"
    }

    companion object
    {
        val Default: XfColor
            get() = XfColor(-1.0, -1.0, -1.0, -1.0, Mode.Default)

        var Accent: XfColor = Default
            internal set

        fun SetAccent(value: XfColor)
        {
            Accent = value
        }

        fun FromHex(hex: String): XfColor
        {
            // Undefined
            if (hex.length < 3)
                return Default
            var idx = if (hex[0] == '#') 1 else 0

            when (hex.length - idx)
            {
                3 -> // #rgb => ffrrggbb
                {
                    val t1 = ToHexD(hex[idx++])
                    val t2 = ToHexD(hex[idx++])
                    val t3 = ToHexD(hex[idx])

                    return FromRgb(t1.toInt(), t2.toInt(), t3.toInt())
                }

                4 -> // #argb => aarrggbb
                {
                    val f1 = ToHexD(hex[idx++])
                    val f2 = ToHexD(hex[idx++])
                    val f3 = ToHexD(hex[idx++])
                    val f4 = ToHexD(hex[idx])
                    return FromRgba(f2.toInt(), f3.toInt(), f4.toInt(), f1.toInt())
                }

                6 -> // #rrggbb => ffrrggbb
                {
                    return FromRgb(
                        (ToHex(hex[idx++]) shl 4 or ToHex(hex[idx++])).toInt(),
                        (ToHex(hex[idx++]) shl 4 or ToHex(hex[idx++])).toInt(),
                        (ToHex(hex[idx++]) shl 4 or ToHex(hex[idx])).toInt()
                    )
                }

                8 -> // #aarrggbb
                {
                    val a1 = ToHex(hex[idx++]) shl 4 or ToHex(hex[idx++])
                    return FromRgba(
                        (ToHex(hex[idx++]) shl 4 or ToHex(hex[idx++])).toInt(),
                        (ToHex(hex[idx++]) shl 4 or ToHex(hex[idx++])).toInt(),
                        (ToHex(hex[idx++]) shl 4 or ToHex(hex[idx])).toInt(),
                        a1.toInt()
                    )
                }

                else -> // everything else will result in unexpected results
                    return Default
            }
        }

        fun FromUint(argb: UInt): XfColor
        {
            return FromRgba(
                ((argb and 0x00ff0000u) shr 0x10).toByte().toInt(),
                ((argb and 0x0000ff00u) shr 0x8).toByte().toInt(),
                (argb and 0x000000ffu).toByte().toInt(),
                ((argb and 0xff000000u) shr 0x18).toByte().toInt()
            )
        }

        fun FromRgba(r: Int, g: Int, b: Int, a: Int): XfColor
        {
            val red = r.toDouble() / 255
            val green = g.toDouble() / 255
            val blue = b.toDouble() / 255
            val alpha = a.toDouble() / 255
            return XfColor(red, green, blue, alpha, Mode.Rgb)
        }

        fun FromRgb(r: Int, g: Int, b: Int): XfColor
        {
            return FromRgba(r, g, b, 255)
        }

        fun FromRgba(r: Double, g: Double, b: Double, a: Double): XfColor
        {
            return XfColor(r, g, b, a)
        }

        fun FromRgb(r: Double, g: Double, b: Double): XfColor
        {
            return XfColor(r, g, b, 1.0, Mode.Rgb)
        }

        fun FromHsla(h: Double, s: Double, l: Double, a: Double = 1.0): XfColor
        {
            return XfColor(h, s, l, a, Mode.Hsl)
        }

        fun FromHsva(h: Double, s: Double, v: Double, a: Double): XfColor
        {
            val hClamped = Clamp(h, 0.0, 1.0)
            val sClamped = Clamp(s, 0.0, 1.0)
            val vClamped = Clamp(v, 0.0, 1.0)
            val range = (floor(hClamped * 6).toInt()) % 6
            val f = hClamped * 6 - floor(hClamped * 6)
            val p = vClamped * (1 - sClamped)
            val q = vClamped * (1 - f * sClamped)
            val t = vClamped * (1 - (1 - f) * sClamped)

            when (range)
            {
                0 -> return FromRgba(vClamped, t, p, a)
                1 -> return FromRgba(q, vClamped, p, a)
                2 -> return FromRgba(p, vClamped, t, a)
                3 -> return FromRgba(p, q, vClamped, a)
                4 -> return FromRgba(t, p, vClamped, a)
            }
            return FromRgba(vClamped, p, q, a)
        }

        fun FromHsv(h: Double, s: Double, v: Double): XfColor
        {
            return FromHsva(h, s, v, 1.0)
        }

        fun FromHsva(h: Int, s: Int, v: Int, a: Int): XfColor
        {
            return FromHsva(h / 360.0, s / 100.0, v / 100.0, a / 100.0)
        }

        fun FromHsv(h: Int, s: Int, v: Int): XfColor
        {
            return FromHsva(h / 360.0, s / 100.0, v / 100.0, 1.0)
        }

        fun Clamp(self: Double, min: Double, max: Double): Double
        {
            if (max < min)
            {
                return max
            }
            else if (self < min)
            {
                return min
            }
            else if (self > max)
            {
                return max
            }

            return self
        }

        inline fun Clamp(self: Int, min: Int, max: Int): Int
        {
            if (max < min)
            {
                return max
            }
            else if (self < min)
            {
                return min
            }
            else if (self > max)
            {
                return max
            }

            return self
        }

        private fun ToHex(c: Char): UInt
        {
            val x = c.code.toUShort()
            if (x >= '0'.code.toUShort() && x <= '9'.code.toUShort())
                return (x - '0'.code.toUShort()).toUInt()

            val xOr = x or 0x20u
            if (xOr >= 'a'.code.toUShort() && xOr <= 'f'.code.toUShort())
                return (xOr - 'a'.code.toUShort() + 10u).toUInt()
            return 0u
        }

        private fun ToHexD(c: Char): UInt
        {
            val j = ToHex(c)
            return (j shl 4) or j
        }

        private fun ConvertToRgb(hue: Float, saturation: Float, luminosity: Float, mode: Mode): RgbResult
        {
            if (mode != Mode.Hsl)
                throw IllegalStateException()

            if (luminosity == 0f)
            {
                return RgbResult(0f, 0f, 0f)
            }

            if (saturation == 0f)
            {
                return RgbResult(luminosity, luminosity, luminosity)
            }
            val temp2 = if (luminosity <= 0.5f) luminosity * (1.0f + saturation) else luminosity + saturation - luminosity * saturation
            val temp1 = 2.0f * luminosity - temp2

            val t3 = floatArrayOf(hue + 1.0f / 3.0f, hue, hue - 1.0f / 3.0f)
            val clr = floatArrayOf(0f, 0f, 0f)
            for (i in 0 until 3)
            {
                if (t3[i] < 0)
                    t3[i] += 1.0f
                if (t3[i] > 1)
                    t3[i] -= 1.0f
                if (6.0 * t3[i] < 1.0)
                    clr[i] = temp1 + (temp2 - temp1) * t3[i] * 6.0f
                else if (2.0 * t3[i] < 1.0)
                    clr[i] = temp2
                else if (3.0 * t3[i] < 2.0)
                    clr[i] = temp1 + (temp2 - temp1) * (2.0f / 3.0f - t3[i]) * 6.0f
                else
                    clr[i] = temp1
            }

            return RgbResult(clr[0], clr[1], clr[2])
        }

        private fun ConvertToHsl(r: Float, g: Float, b: Float, mode: Mode): HslResult
        {
            var v = max(r, g)
            v = max(v, b)

            var m = min(r, g)
            m = min(m, b)

            val l = (m + v) / 2.0f
            if (l <= 0.0)
            {
                return HslResult(0f, 0f, 0f)
            }
            val vm = v - m
            var s = vm

            if (s > 0.0)
            {
                s /= if (l <= 0.5f) v + m else 2.0f - v - m
            }
            else
            {
                return HslResult(0f, 0f, l)
            }

            val r2 = (v - r) / vm
            val g2 = (v - g) / vm
            val b2 = (v - b) / vm

            var h: Float
            if (r == v)
            {
                h = if (g == m) 5.0f + b2 else 1.0f - g2
            }
            else if (g == v)
            {
                h = if (b == m) 1.0f + r2 else 3.0f - b2
            }
            else
            {
                h = if (r == m) 3.0f + g2 else 5.0f - r2
            }
            h /= 6.0f

            return HslResult(h, s, l)
        }

        private data class RgbResult(val r: Float, val g: Float, val b: Float)
        private data class HslResult(val h: Float, val s: Float, val l: Float)

        val Black = XfColor(0, 0, 0)
        val Red = XfColor(255, 0, 0)
        val White = XfColor(255, 255, 255)
    }
}