package datastructures

import kotlin.math.absoluteValue
import kotlin.math.sqrt
import utils.map
import utils.match
import utils.pow
import utils.toPair

data class Point2D(val x: Int, val y: Int) {
    companion object {
        val ZERO = Point2D(0, 0)
    }

    val absoluteValue
        get(): Point2D = Point2D(this.x.absoluteValue, this.y.absoluteValue)

    val top
        get() = Point2D(x, y - 1)
    val topLeft
        get() = Point2D(x - 1, y - 1)
    val topRight
        get() = Point2D(x + 1, y - 1)

    val left
        get() = Point2D(x - 1, y)
    val right
        get() = Point2D(x + 1, y)

    val bottom
        get() = Point2D(x, y + 1)
    val bottomLeft
        get() = Point2D(x - 1, y + 1)
    val bottomRight
        get() = Point2D(x + 1, y + 1)

    val neighboursOrthogonally
        get() = listOf(left, right, top, bottom)
    val neighbours
        get() = listOf(left, topLeft, top, topRight, right, bottomRight, bottom, bottomLeft)

    operator fun minus(other: Point2D): Point2D = Point2D(this.x - other.x, this.y - other.y)
    operator fun plus(other: Point2D): Point2D = Point2D(this.x + other.x, this.y + other.y)

    fun distanceTo(other: Point2D): Double =
        sqrt((other.x - this.x).pow(2).toDouble() + (other.y - this.y).pow(2))

    override fun toString() = "($x,$y)"
}

fun parsePoint2D(input: String) =
    input.match("""(-?\d+),(-?\d+)""").toPair().map { it.toInt() }.let { (x, y) -> Point2D(x, y) }
