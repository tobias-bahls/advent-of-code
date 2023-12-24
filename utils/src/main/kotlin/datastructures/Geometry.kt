package datastructures

import datastructures.CardinalDirection.*
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import utils.cartesian
import utils.map
import utils.match
import utils.pow
import utils.toPair
import utils.toTriple

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

    operator fun times(other: Int): Point2D = Point2D(this.x * other, this.y * other)

    fun distanceTo(other: Point2D): Double =
        sqrt((other.x - this.x).pow(2).toDouble() + (other.y - this.y).pow(2))

    fun manhattanDistanceTo(other: Point2D): Int =
        (this.x - other.x).absoluteValue + (this.y - other.y).absoluteValue

    fun rotate(pivot: Point2D, angle: Int): Point2D = (this - pivot).rotate(angle) + pivot

    fun rotate(angle: Int): Point2D {
        val angleRad = Math.toRadians(angle.toDouble())
        val s = sin(angleRad)
        val c = cos(angleRad)

        return Point2D((x * c - y * s).roundToInt(), (x * s + y * c).roundToInt())
    }

    fun angleTo(other: Point2D): Double {
        val d = Math.toDegrees(atan2((other.y - y).toDouble(), (other.x - x).toDouble())) + 90
        return if (d < 0) d + 360 else d
    }

    fun neighbourInDirection(dir: CardinalDirectionOrthogonal) =
        when (dir) {
            East -> right
            North -> top
            South -> bottom
            West -> left
        }

    override fun toString() = "($x,$y)"
}

data class Rectangle(val position: Point2D, val width: Int, val height: Int) {
    val x1 = position.x
    val x2 = position.x + width

    val y1 = position.y
    val y2 = position.y + height

    val xRange = (x1 until x2)
    val yRange = (y1 until y2)

    val points by lazy { xRange.flatMap { x -> yRange.map { y -> Point2D(x, y) } } }
}

data class Line(val from: Point2D, val to: Point2D) {
    fun contains(point: Point2D) =
        point.x in minOf(from.x, to.x)..maxOf(from.x, to.x) &&
            point.y in minOf(from.y, to.y)..maxOf(from.y, to.y)
}

fun parsePoint2D(input: String) =
    input.match("""(-?\d+),(-?\d+)""").toPair().map { it.toInt() }.let { (x, y) -> Point2D(x, y) }

data class Point3D(val x: Int, val y: Int, val z: Int) {
    companion object {
        val ZERO = Point3D(0, 0, 0)

        val allNeighbourVectors =
            listOf(-1, 0, 1)
                .let { it.cartesian(it).cartesian(it) }
                .map { it.toTriple() }
                .map { (x, y, z) -> Point3D(x, y, z) }
                .filter { it != ZERO }
                .toList()

        val orthogonalNeighbourVectors =
            listOf(
                Point3D(-1, 0, 0),
                Point3D(1, 0, 0),
                Point3D(0, 1, 0),
                Point3D(0, -1, 0),
                Point3D(0, 0, 1),
                Point3D(0, 0, -1),
            )
    }

    fun transform(matrix: Array<IntArray>): Point3D {
        return Point3D(
            matrix[0][0] * x + matrix[0][1] * y + matrix[0][2] * z,
            matrix[1][0] * x + matrix[1][1] * y + matrix[1][2] * z,
            matrix[2][0] * x + matrix[2][1] * y + matrix[2][2] * z,
        )
    }

    val neighbours
        get() = allNeighbourVectors.map { this + it }

    val neighboursOrthogonally
        get() = orthogonalNeighbourVectors.map { this + it }

    val absoluteValue
        get(): Point3D = Point3D(x.absoluteValue, y.absoluteValue, z.absoluteValue)

    val components
        get() = listOf(x, y, z)

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

    fun manhattanDistanceTo(other: Point3D): Int =
        (this.x - other.x).absoluteValue +
            (this.y - other.y).absoluteValue +
            (this.z - other.z).absoluteValue

    operator fun plus(other: Point3D) = translate(other)

    operator fun minus(other: Point3D) = translate(other.negate())

    override fun toString() = "($x,$y,$z)"
}

val CardinalDirection.movementVector
    get() =
        when (this) {
            East -> Point2D(1, 0)
            North -> Point2D(0, -1)
            South -> Point2D(0, 1)
            West -> Point2D(-1, 0)
            NorthEast -> Point2D(1, -1)
            NorthWest -> Point2D(-1, -1)
            SouthEast -> Point2D(1, 1)
            SouthWest -> Point2D(-1, 1)
        }

fun parsePoint3D(input: String) =
    input
        .match("""(-?\d+),\s*(-?\d+),\s*(-?\d+)""")
        .toList()
        .map { it.toInt() }
        .let { (x, y, z) -> Point3D(x, y, z) }
