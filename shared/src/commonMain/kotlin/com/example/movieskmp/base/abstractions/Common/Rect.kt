package com.base.abstractions.Common


import kotlin.math.min
import kotlin.math.max
import kotlin.math.round

data class Rect(
    var X: Double = 0.0,
    var Y: Double = 0.0,
    var Width: Double = 0.0,
    var Height: Double = 0.0
) {
    constructor(loc: Point, sz: Size) : this(loc.X, loc.Y, sz.Width, sz.Height)

    val Top: Double get() = Y

    val Bottom: Double get() = Y + Height

    val Right: Double get() = X + Width

    val Left: Double get() = X

    val IsEmpty: Boolean get() = (Width <= 0) || (Height <= 0)

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

    val Center: Point get() = Point(X + Width / 2, Y + Height / 2)

    fun Equals(other: Rect): Boolean = X.equals(other.X) && Y.equals(other.Y) && Width.equals(other.Width) && Height.equals(other.Height)

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false

        return other is Rect && Equals(other) || other is Rectangle && Equals(other)
    }

    fun Equals(other: Rectangle): Boolean = X.equals(other.X) && Y.equals(other.Y) && Width.equals(other.Width) && Height.equals(other.Height)

    override fun hashCode(): Int {
        var hashCode = X.hashCode()
        hashCode = (hashCode * 397) xor Y.hashCode()
        hashCode = (hashCode * 397) xor Width.hashCode()
        hashCode = (hashCode * 397) xor Height.hashCode()
        return hashCode
    }

    // Hit Testing / Intersection / Union
    fun Contains(rect: Rect): Boolean = X <= rect.X && Right >= rect.Right && Y <= rect.Y && Bottom >= rect.Bottom

    fun Contains(pt: Point): Boolean = Contains(pt.X, pt.Y)

    fun Contains(x: Double, y: Double): Boolean = (x >= Left) && (x < Right) && (y >= Top) && (y < Bottom)

    fun IntersectsWith(r: Rect): Boolean = !((Left >= r.Right) || (Right <= r.Left) || (Top >= r.Bottom) || (Bottom <= r.Top))

    fun Union(r: Rect): Rect = Union(this, r)

    fun Intersect(r: Rect): Rect = Intersect(this, r)

    // Inflate and Offset
    fun Inflate(sz: Size): Rect = Inflate(sz.Width, sz.Height)

    fun Inflate(width: Double, height: Double): Rect {
        val r = this.copy()
        r.X -= width
        r.Y -= height
        r.Width += width * 2
        r.Height += height * 2
        return r
    }

    fun Offset(dx: Double, dy: Double): Rect {
        val r = this.copy()
        r.X += dx
        r.Y += dy
        return r
    }

    fun Offset(dr: Point): Rect = Offset(dr.X, dr.Y)

    fun Round(): Rect = Rect(round(X), round(Y), round(Width), round(Height))

    fun Deconstruct(): DeconstructedRect {
        return DeconstructedRect(X, Y, Width, Height)
    }

    override fun toString(): String = "{X=$X Y=$Y Width=$Width Height=$Height}"

    companion object {
        var Zero = Rect()

        fun FromLTRB(left: Double, top: Double, right: Double, bottom: Double): Rect = Rect(left, top, right - left, bottom - top)

        fun Union(r1: Rect, r2: Rect): Rect = FromLTRB(min(r1.Left, r2.Left), min(r1.Top, r2.Top), max(r1.Right, r2.Right), max(r1.Bottom, r2.Bottom))

        fun Intersect(r1: Rect, r2: Rect): Rect {
            val x = max(r1.X, r2.X)
            val y = max(r1.Y, r2.Y)
            val width = min(r1.Right, r2.Right) - x
            val height = min(r1.Bottom, r2.Bottom) - y

            if (width < 0 || height < 0)
                return Zero

            return Rect(x, y, width, height)
        }
    }
}

data class DeconstructedRect(val x: Double, val y: Double, val width: Double, val height: Double)

//operator fun Rect.component1(): Double = X
//operator fun Rect.component2(): Double = Y
//operator fun Rect.component3(): Double = Width
//operator fun Rect.component4(): Double = Height


