package datastructures

import kotlin.math.absoluteValue
import kotlin.math.sqrt
import utils.Scored
import utils.genericDijkstra
import utils.map
import utils.match
import utils.parseGrid
import utils.pow
import utils.toPair

data class Tile<T>(val grid: Grid<T>, val point: Point, val data: T) {
    val above: Tile<T>?
        get() = grid.tileAt(point.top)

    val right: Tile<T>?
        get() = grid.tileAt(point.right)

    val below: Tile<T>?
        get() = grid.tileAt(point.bottom)

    val left: Tile<T>?
        get() = grid.tileAt(point.left)

    fun adjacentOrthogonally(): List<Tile<T>> =
        point.neighboursOrthogonally.mapNotNull { grid.tileAt(it) }
    fun adjacent(): List<Tile<T>> = point.neighbours.mapNotNull { grid.tileAt(it) }

    fun isEdge(): Boolean {
        if (point.x == 0 || point.y == 0) {
            return true
        }

        if (point.x == grid.width || point.y == grid.height) {
            return true
        }

        return false
    }

    override fun toString(): String {
        return "Tile(point=$point, data=$data)"
    }
}

data class Point(val x: Int, val y: Int) {
    companion object {
        val ZERO = Point(0, 0)
    }

    val absoluteValue
        get(): Point = Point(this.x.absoluteValue, this.y.absoluteValue)

    val top
        get() = Point(x, y - 1)
    val topLeft
        get() = Point(x - 1, y - 1)
    val topRight
        get() = Point(x + 1, y - 1)

    val left
        get() = Point(x - 1, y)
    val right
        get() = Point(x + 1, y)

    val bottom
        get() = Point(x, y + 1)
    val bottomLeft
        get() = Point(x - 1, y + 1)
    val bottomRight
        get() = Point(x + 1, y + 1)

    val neighboursOrthogonally
        get() = listOf(left, right, top, bottom)
    val neighbours
        get() = listOf(left, topLeft, top, topRight, right, bottomRight, bottom, bottomLeft)

    operator fun minus(other: Point): Point = Point(this.x - other.x, this.y - other.y)
    operator fun plus(other: Point): Point = Point(this.x + other.x, this.y + other.y)

    fun distanceTo(other: Point): Double =
        sqrt((other.x - this.x).pow(2).toDouble() + (other.y - this.y).pow(2))

    override fun toString() = "($x,$y)"
}

fun parsePoint(input: String) =
    input.match("""(-?\d+),(-?\d+)""").toPair().map { it.toInt() }.let { (x, y) -> Point(x, y) }

class Grid<T>(input: String, createTile: (Char) -> T) {
    private val _tiles: MutableMap<Point, Tile<T>>

    val tiles: Collection<Tile<T>>
        get() = _tiles.values

    val width: Int
        get() = _tiles.values.maxOf { it.point.x } + 1
    val height: Int
        get() = _tiles.values.maxOf { it.point.y } + 1

    init {
        this._tiles =
            parseGrid(input) { x, y, char -> Tile(this, Point(x, y), createTile(char)) }
                .associateBy { it.point }
                .toMutableMap()
    }

    fun tileAt(point: Point) = _tiles[point]

    fun addTile(point: Point, data: T) {
        this._tiles[point] = Tile(this, point, data)
    }

    fun dijkstra(start: Tile<T>, end: Tile<T>, score: (Tile<T>, Tile<T>) -> Int) =
        genericDijkstra(
            start = start,
            end = end,
            neighbours = { tile ->
                tile.adjacentOrthogonally().map { neighbour ->
                    Scored(score(tile, neighbour), neighbour)
                }
            })

    fun dijkstra(start: Tile<T>, end: Tile<T>, neighbours: (Tile<T>) -> List<Scored<Tile<T>>>) =
        genericDijkstra(start = start, end = end, neighbours = neighbours)

    override fun toString(): String {
        return "Grid(tiles=$tiles)"
    }
}

class SparseGrid(val points: Set<Point>) {
    val width: Int = points.maxOf { it.x }
    val height: Int = points.maxOf { it.y }

    fun hasPoint(x: Int, y: Int): Boolean = points.contains(Point(x, y))
}
