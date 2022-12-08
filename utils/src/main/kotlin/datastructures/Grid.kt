package datastructures

import utils.filterNotBlank
import utils.map
import utils.match
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
}

fun parsePoint(input: String) =
    input.match("""(\d+),(\d+)""").toPair().map { it.toInt() }.let { (x, y) -> Point(x, y) }

class Grid<T>(input: String, createTile: (Char) -> T) {
    val tiles: List<Tile<T>>
    val width: Int
    val height: Int

    init {
        val tiles =
            input.lines().filterNotBlank().flatMapIndexed { y, row ->
                row.toCharArray().mapIndexed { x, char ->
                    Tile(this, Point(x, y), createTile(char))
                }
            }

        this.tiles = tiles
        this.width = tiles.maxOf { it.point.x }
        this.height = tiles.maxOf { it.point.y }
    }

    fun tileAt(point: Point) = tiles.firstOrNull { it.point == point }

    override fun toString(): String {
        return "Grid(tiles=$tiles)"
    }
}

class SparseGrid(val points: Set<Point>) {
    val width: Int = points.maxOf { it.x }
    val height: Int = points.maxOf { it.y }

    fun hasPoint(x: Int, y: Int): Boolean = points.contains(Point(x, y))
}
