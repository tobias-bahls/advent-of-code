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

    fun manhattanDistanceTo(other: Point2D): Int =
        (this.x - other.x).absoluteValue + (this.y - other.y).absoluteValue

    override fun toString() = "($x,$y)"
}

data class Line(val from: Point2D, val to: Point2D)

fun parsePoint2D(input: String) =
    input.match("""(-?\d+),(-?\d+)""").toPair().map { it.toInt() }.let { (x, y) -> Point2D(x, y) }

data class Point3D(val x: Int, val y: Int, val z: Int) {
    fun rotate(rotationMatrix: Array<IntArray>): Point3D {
        return Point3D(
            rotationMatrix[0][0] * x + rotationMatrix[0][1] * y + rotationMatrix[0][2] * z,
            rotationMatrix[1][0] * x + rotationMatrix[1][1] * y + rotationMatrix[1][2] * z,
            rotationMatrix[2][0] * x + rotationMatrix[2][1] * y + rotationMatrix[2][2] * z,
        )
    }

    val neighboursOrthogonally
        get() =
            listOf(
                    Point3D(-1, 0, 0),
                    Point3D(1, 0, 0),
                    Point3D(0, 1, 0),
                    Point3D(0, -1, 0),
                    Point3D(0, 0, 1),
                    Point3D(0, 0, -1),
                )
                .map { this + it }

    fun translate(translation: Point3D): Point3D {
        return Point3D(
            this.x + translation.x,
            this.y + translation.y,
            this.z + translation.z,
        )
    }

    fun negate(): Point3D {
        return Point3D(-x, -y, -z)
    }

    operator fun plus(other: Point3D) = translate(other)
    operator fun minus(other: Point3D) = translate(other.negate())

    override fun toString() = "($x,$y,$z)"
}

fun parsePoint3D(input: String) =
    input
        .match("""(-?\d+),(-?\d+),(-?\d+)""")
        .toList()
        .map { it.toInt() }
        .let { (x, y, z) -> Point3D(x, y, z) }
