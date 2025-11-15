package com.base.abstractions.Common

import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.round

class Point {
    var X: Double
    var Y: Double

    companion object {
        var Zero = Point()
    }

    override fun toString(): String {
        return "{X=$X Y=$Y}"
    }

    constructor(x: Double = 0.0, y: Double = 0.0) {
        this.X = x
        this.Y = y
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Point)
            return false

        return (this.X == other.X) && (this.Y == other.Y)
    }

    override fun hashCode(): Int {
        return X.hashCode() xor (Y.hashCode() * 397)
    }

    fun Offset(dx: Double, dy: Double): Point {
        val p = Point(this.X, this.Y)
        p.X += dx
        p.Y += dy
        return p
    }

    fun Round(): Point {
        return Point(round(X), round(Y))
    }

    val IsEmpty: Boolean
        get() = (X == 0.0) && (Y == 0.0)

    fun toSize(): Size {
        return Size(X, Y)
    }

    operator fun plus(sz: Size): Point {
        return Point(X + sz.Width, Y + sz.Height)
    }

    operator fun minus(sz: Size): Point {
        return Point(X - sz.Width, Y - sz.Height)
    }

    fun Distance(other: Point): Double {
        return sqrt((X - other.X).pow(2.0) + (Y - other.Y).pow(2.0))
    }

    operator fun component1(): Double = X
    operator fun component2(): Double = Y
}

