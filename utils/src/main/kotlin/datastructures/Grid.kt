package datastructures

import algorithms.dijkstraPath
import datastructures.CardinalDirection.East
import datastructures.CardinalDirection.North
import datastructures.CardinalDirection.South
import datastructures.CardinalDirection.West
import kotlin.math.absoluteValue
import utils.Scored
import utils.filterNotBlank
import utils.toRangeBy

sealed interface CardinalDirection {
    sealed interface CardinalDirectionOrthogonal : CardinalDirection {
        companion object {
            val ALL = listOf(North, East, South, West)
        }
    }

    object North : CardinalDirectionOrthogonal

    object NorthEast : CardinalDirection

    object East : CardinalDirectionOrthogonal

    object SouthEast : CardinalDirection

    object South : CardinalDirectionOrthogonal

    object SouthWest : CardinalDirection
    object West : CardinalDirectionOrthogonal

    object NorthWest : CardinalDirection
}

data class Tile<T>(val point: Point2D, val data: T) {
    lateinit var grid: Grid<T>

    val north: Tile<T>?
        get() = neighbourInDirection(North)

    val east: Tile<T>?
        get() = neighbourInDirection(East)

    val south: Tile<T>?
        get() = neighbourInDirection(South)

    val west: Tile<T>?
        get() = neighbourInDirection(West)

    fun adjacentOrthogonally(): List<Tile<T>> =
        point.neighboursOrthogonally.mapNotNull { grid.tileAt(it) }
    fun adjacent(): List<Tile<T>> = point.neighbours.mapNotNull { grid.tileAt(it) }

    fun neighboursInDirections(directions: List<CardinalDirection>): List<Tile<T>> =
        directions.mapNotNull { neighbourInDirection(it) }

    fun neighbourInDirection(cardinalDirection: CardinalDirection): Tile<T>? =
        pointInCardinalDirection(cardinalDirection).let { grid.tileAt(it) }

    fun pointInCardinalDirection(cardinalDirection: CardinalDirection) =
        when (cardinalDirection) {
            East -> point.right
            North -> point.top
            South -> point.bottom
            West -> point.left
            CardinalDirection.NorthEast -> point.topRight
            CardinalDirection.NorthWest -> point.topLeft
            CardinalDirection.SouthEast -> point.bottomRight
            CardinalDirection.SouthWest -> point.bottomLeft
        }

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

class Grid<T>(tiles: List<Tile<T>>) {
    private val _tiles: MutableMap<Point2D, Tile<T>>

    init {
        _tiles = tiles.associateBy { it.point }.toMutableMap()
        _tiles.values.onEach { it.grid = this }
    }

    val tiles: Collection<Tile<T>>
        get() = _tiles.values

    val width: Int
        get() =
            _tiles.values.maxOf { it.point.x } +
                _tiles.values.minOf { it.point.x }.absoluteValue +
                1
    val height: Int
        get() =
            _tiles.values.maxOf { it.point.y } +
                _tiles.values.minOf { it.point.y }.absoluteValue +
                1

    val xRange: ClosedRange<Int>
        get() = _tiles.values.toRangeBy { it.point.x }

    val yRange: ClosedRange<Int>
        get() = _tiles.values.toRangeBy { it.point.y }

    fun rowBounds(row: Int) = tiles.filter { it.point.y == row }.toRangeBy { it.point.x }

    fun columnBounds(column: Int) = tiles.filter { it.point.x == column }.toRangeBy { it.point.y }

    fun tileAt(point: Point2D) = _tiles[point]

    fun addTile(point: Point2D, data: T) {
        val tile = Tile(point, data)
        tile.grid = this
        this._tiles[point] = tile
    }

    fun dijkstra(start: Tile<T>, end: Tile<T>, score: (Tile<T>, Tile<T>) -> Int) =
        dijkstraPath<Tile<T>> {
            this.start = start
            this.end = end

            neighbours { tile ->
                tile.adjacentOrthogonally().map { neighbour ->
                    Scored(score(tile, neighbour), neighbour)
                }
            }
        }

    fun dijkstra(start: Tile<T>, end: Tile<T>, neighbours: (Tile<T>) -> List<Scored<Tile<T>>>) =
        dijkstraPath<Tile<T>> {
            this.start = start
            this.end = end

            neighbours { neighbours(it) }
        }

    override fun toString(): String {
        return "Grid(tiles=$tiles)"
    }
}

fun <T> parseGridWithEmptyTiles(input: String, tileData: (char: Char) -> T): Grid<T> {
    val tiles =
        input.lines().flatMapIndexed { y, row ->
            row.toCharArray()
                .mapIndexed { x, char ->
                    if (char == ' ') return@mapIndexed null

                    Tile(Point2D(x, y), tileData(char))
                }
                .filterNotNull()
        }

    return Grid(tiles)
}

fun <T> parseAsciiGrid(input: String, processTile: (x: Int, y: Int, char: Char) -> T?): List<T> =
    input.lines().filterNotBlank().flatMapIndexed { y, row ->
        row.toCharArray().mapIndexed { x, char -> processTile(x, y, char) }.filterNotNull()
    }

fun <T> parseGrid(input: String, tileData: (char: Char) -> T?): Grid<T> {
    val tiles =
        parseAsciiGrid(input) { x, y, char ->
            val data = tileData(char) ?: return@parseAsciiGrid null

            Tile(Point2D(x, y), data as T)
        }

    return Grid(tiles)
}

class SparseGrid(initialPoints: Set<Point2D>) {
    private var _points: MutableSet<Point2D>
    init {
        _points = initialPoints.toMutableSet()
    }
    val width: Int = points.maxOf { it.x }
    val height: Int = points.maxOf { it.y }

    val points: Set<Point2D>
        get() = _points

    fun hasPoint(x: Int, y: Int): Boolean = hasPoint(Point2D(x, y))
    fun hasPoint(point: Point2D): Boolean = points.contains(point)

    fun addPoint(point: Point2D) {
        _points.add(point)
    }
}
