package com.base.abstractions.Common

import kotlin.math.min
import kotlin.math.max
import kotlin.math.round

data class Rectangle(
    var X: Double = 0.0,
    var Y: Double = 0.0,
    var Width: Double = 0.0,
    var Height: Double = 0.0
) {
    companion object {
        val Zero = Rectangle()

        fun FromLTRB(left: Double, top: Double, right: Double, bottom: Double): Rectangle {
            return Rectangle(left, top, right - left, bottom - top)
        }

        fun Union(r1: Rectangle, r2: Rectangle): Rectangle {
            return FromLTRB(min(r1.Left, r2.Left), min(r1.Top, r2.Top), max(r1.Right, r2.Right), max(r1.Bottom, r2.Bottom))
        }

        fun Intersect(r1: Rectangle, r2: Rectangle): Rectangle {
            val x = max(r1.X, r2.X)
            val y = max(r1.Y, r2.Y)
            val width = min(r1.Right, r2.Right) - x
            val height = min(r1.Bottom, r2.Bottom) - y

            if (width < 0 || height < 0) {
                return Zero
            }
            return Rectangle(x, y, width, height)
        }
    }

    constructor(loc: Point, sz: Size) : this(loc.X, loc.Y, sz.Width, sz.Height) {
    }

    override fun toString(): String {
        return "{X=$X Y=$Y Width=$Width Height=$Height}"
    }

    fun Equals(other: Rectangle): Boolean {
        return X == other.X && Y == other.Y && Width == other.Width && Height == other.Height
    }

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false

        return when (other) {
            is Rectangle -> Equals(other)
            is Rect -> X == other.X && Y == other.Y && Width == other.Width && Height == other.Height
            else -> false
        }
    }

    override fun hashCode(): Int {
        var hashCode = X.hashCode()
        hashCode = (hashCode * 397) xor Y.hashCode()
        hashCode = (hashCode * 397) xor Width.hashCode()
        hashCode = (hashCode * 397) xor Height.hashCode()
        return hashCode
    }

    // Hit Testing / Intersection / Union
    fun Contains(rect: Rectangle): Boolean {
        return X <= rect.X && Right >= rect.Right && Y <= rect.Y && Bottom >= rect.Bottom
    }

    fun Contains(pt: Point): Boolean {
        return Contains(pt.X, pt.Y)
    }

    fun Contains(x: Double, y: Double): Boolean {
        return (x >= Left) && (x < Right) && (y >= Top) && (y < Bottom)
    }

    fun IntersectsWith(r: Rectangle): Boolean {
        return !((Left >= r.Right) || (Right <= r.Left) || (Top >= r.Bottom) || (Bottom <= r.Top))
    }

    fun Union(r: Rectangle): Rectangle {
        return Union(this, r)
    }

    fun Intersect(r: Rectangle): Rectangle {
        return Intersect(this, r)
    }

    // Position/Size
    var Top: Double
        get() = Y
        set(value) { Y = value }

    var Bottom: Double
        get() = Y + Height
        set(value) { Height = value - Y }

    var Right: Double
        get() = X + Width
        set(value) { Width = value - X }

    var Left: Double
        get() = X
        set(value) { X = value }

    val IsEmpty: Boolean
        get() = (Width <= 0) || (Height <= 0)

    var Size: Size
        get() = Size(Width, Height)
        set(value) {
            Width = value.Width
            Height = value.Height
        }

    var Location: Point
        get() = Point(X, Y)
        set(value) {
            X = value.X
            Y = value.Y
        }

    val Center: Point
        get() = Point(X + Width / 2, Y + Height / 2)

    // Inflate and Offset
    fun Inflate(sz: Size): Rectangle {
        return Inflate(sz.Width, sz.Height)
    }

    fun Inflate(width: Double, height: Double): Rectangle {
        val r = this.copy()
        r.X -= width
        r.Y -= height
        r.Width += width * 2
        r.Height += height * 2
        return r
    }

    fun Offset(dx: Double, dy: Double): Rectangle {
        val r = this.copy()
        r.X += dx
        r.Y += dy
        return r
    }

    fun Offset(dr: Point): Rectangle {
        return Offset(dr.X, dr.Y)
    }

    fun Round(): Rectangle {
        return Rectangle(round(X), round(Y), round(Width), round(Height))
    }

//    operator fun component1(): Double = X
//    operator fun component2(): Double = Y
//    operator fun component3(): Double = width
//    operator fun component4(): Double = Height

    fun toRect(): Rect = Rect(X, Y, Width, Height)
}

fun Rect.toRectangle(): Rectangle = Rectangle(X, Y, Width, Height)


