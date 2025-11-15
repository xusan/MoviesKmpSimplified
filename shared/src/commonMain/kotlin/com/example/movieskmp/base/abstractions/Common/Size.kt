package com.base.abstractions.Common

class Size {
    private var _width: Double = 0.0
    private var _height: Double = 0.0

    companion object {
        val Zero = Size(0.0, 0.0)
    }

    constructor(width: Double, height: Double) {
        if (width.isNaN())
            throw IllegalArgumentException("NaN is not a valid value for width")
        if (height.isNaN())
            throw IllegalArgumentException("NaN is not a valid value for height")
        _width = width
        _height = height
    }

    val isZero: Boolean
        get() = (_width == 0.0) && (_height == 0.0)

    var Width: Double
        get() = _width
        set(value) {
            if (value.isNaN())
                throw IllegalArgumentException("NaN is not a valid value for Width")
            _width = value
        }

    var Height: Double
        get() = _height
        set(value) {
            if (value.isNaN())
                throw IllegalArgumentException("NaN is not a valid value for Height")
            _height = value
        }

    operator fun plus(other: Size): Size {
        return Size(_width + other._width, _height + other._height)
    }

    operator fun minus(other: Size): Size {
        return Size(_width - other._width, _height - other._height)
    }

    operator fun times(value: Double): Size {
        return Size(_width * value, _height * value)
    }

    fun equals(other: Size): Boolean {
        return _width == other._width && _height == other._height
    }

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false
        return other is Size && equals(other)
    }

    override fun hashCode(): Int {
        return (_width.hashCode() * 397) xor _height.hashCode()
    }

    override fun toString(): String {
        return "{Width=$_width Height=$_height}"
    }

    fun toPoint(): Point {
        return Point(Width, Height)
    }

    operator fun component1(): Double = Width
    operator fun component2(): Double = Height
}


