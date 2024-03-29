package datastructures

import algorithms.dijkstraPath
import datastructures.CardinalDirection.East
import datastructures.CardinalDirection.North
import datastructures.CardinalDirection.NorthEast
import datastructures.CardinalDirection.NorthWest
import datastructures.CardinalDirection.South
import datastructures.CardinalDirection.SouthEast
import datastructures.CardinalDirection.SouthWest
import datastructures.CardinalDirection.West
import datastructures.LeftRightDirection.*
import kotlin.math.absoluteValue
import utils.Scored
import utils.filterNotBlank
import utils.repeatUntilTrue
import utils.toRangeBy

sealed interface LeftRightDirection {
    data object Left : LeftRightDirection

    data object Right : LeftRightDirection
}

sealed interface CardinalDirection {
    companion object {
        val ALL = listOf(North, NorthEast, East, SouthEast, South, SouthWest, West, NorthWest)
    }

    sealed interface CardinalDirectionOrthogonal : CardinalDirection {
        companion object {
            val ALL = listOf(North, East, South, West)
        }

        fun turn(leftRightDirection: LeftRightDirection) =
            when (leftRightDirection) {
                Left ->
                    when (this) {
                        North -> West
                        East -> North
                        South -> East
                        West -> South
                    }
                Right ->
                    when (this) {
                        North -> East
                        East -> South
                        South -> West
                        West -> North
                    }
            }

        fun opposite() =
            when (this) {
                North -> South
                East -> West
                South -> North
                West -> East
            }
    }

    data object North : CardinalDirectionOrthogonal

    data object NorthEast : CardinalDirection

    data object East : CardinalDirectionOrthogonal

    data object SouthEast : CardinalDirection

    data object South : CardinalDirectionOrthogonal

    data object SouthWest : CardinalDirection

    data object West : CardinalDirectionOrthogonal

    data object NorthWest : CardinalDirection
}

fun parseCardinalDirection(input: String) =
    when (input.lowercase()) {
        "n" -> North
        "ne" -> NorthEast
        "e" -> East
        "se" -> SouthEast
        "s" -> South
        "sw" -> SouthWest
        "w" -> West
        "nw" -> NorthWest
        else -> error("Unknown cardinal direction string: $input")
    }

data class Tile<T>(val point: Point2D, val data: T) {
    lateinit var grid: Grid<T>

    val north: Tile<T>?
        get() = directNeighbourInDirection(North)

    val east: Tile<T>?
        get() = directNeighbourInDirection(East)

    val south: Tile<T>?
        get() = directNeighbourInDirection(South)

    val west: Tile<T>?
        get() = directNeighbourInDirection(West)

    fun adjacentOrthogonally(): List<Tile<T>> =
        point.neighboursOrthogonally.mapNotNull { grid.tileAt(it) }

    fun adjacent(): List<Tile<T>> = point.neighbours.mapNotNull { grid.tileAt(it) }

    fun directNeighboursInDirections(directions: Collection<CardinalDirection>): List<Tile<T>> =
        directions.mapNotNull { directNeighbourInDirection(it) }

    fun directNeighbourInDirection(cardinalDirection: CardinalDirection): Tile<T>? =
        pointInCardinalDirection(cardinalDirection).let { grid.tileAt(it) }

    fun allTilesInDirection(cardinalDirection: CardinalDirection): Sequence<Tile<T>> {
        var current = point + cardinalDirection.movementVector

        return generateSequence {
            if (!grid.inBounds(current)) {
                return@generateSequence null
            }

            while (grid.inBounds(current)) {
                val tile = grid.tileAt(current)
                current += cardinalDirection.movementVector
                if (tile != null) {
                    return@generateSequence tile
                }
            }

            return@generateSequence null
        }
    }

    fun pointInCardinalDirection(cardinalDirection: CardinalDirection) =
        when (cardinalDirection) {
            East -> point.right
            North -> point.top
            South -> point.bottom
            West -> point.left
            NorthEast -> point.topRight
            NorthWest -> point.topLeft
            SouthEast -> point.bottomRight
            SouthWest -> point.bottomLeft
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

class Grid<T>(tiles: List<Tile<T>>, val width: Int, val height: Int) {
    constructor(
        tiles: List<Tile<T>>
    ) : this(
        tiles,
        tiles.maxOf { it.point.x } + tiles.minOf { it.point.x }.absoluteValue + 1,
        tiles.maxOf { it.point.y } + tiles.minOf { it.point.y }.absoluteValue + 1)

    private val _tiles: MutableMap<Point2D, Tile<T>>

    var wrapAroundX: Boolean = false
    var wrapAroundY: Boolean = false

    init {
        _tiles = tiles.associateBy { it.point }.toMutableMap()
        _tiles.values.onEach { it.grid = this }
    }

    val tiles: Collection<Tile<T>>
        get() = _tiles.values

    val xRange: ClosedRange<Int> by lazy { _tiles.values.toRangeBy { it.point.x } }
    val xRangeProgression: IntProgression = (xRange.start..xRange.endInclusive)

    val yRange: ClosedRange<Int> by lazy { _tiles.values.toRangeBy { it.point.y } }
    val yRangeProgression: IntProgression = (yRange.start..yRange.endInclusive)

    fun rowBounds(row: Int) = row(row).toRangeBy { it.point.x }

    fun row(row: Int) = tiles.filter { it.point.y == row }

    fun rows() = yRangeProgression.map { row(it) }

    fun columnBounds(column: Int) = column(column).toRangeBy { it.point.y }

    fun column(column: Int) = tiles.filter { it.point.x == column }

    fun columns() = xRangeProgression.map { column(it) }

    fun inBounds(point: Point2D) = point.x in xRange && point.y in yRange

    fun tileAt(point: Point2D) = _tiles[wrapAround(point)]

    fun northEdge() = tiles.filter { it.point.y == 0 }

    fun eastEdge() = tiles.filter { it.point.x == width - 1 }

    fun southEdge() = tiles.filter { it.point.y == height - 1 }

    fun westEdge() = tiles.filter { it.point.x == 0 }

    fun edges() = northEdge() + eastEdge() + southEdge() + westEdge()

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

    data class TransformUntilStableResult<T>(val iterations: Int, val resultGrid: Grid<T>)

    fun transformUntilStable(transform: (Grid<T>) -> Grid<T>): TransformUntilStableResult<T> {
        var currentGrid = this
        val iterations =
            repeatUntilTrue {
                val nextGrid = transform(currentGrid)

                if (currentGrid.hasSameTiles(nextGrid)) {
                    true
                } else {
                    currentGrid = nextGrid
                    false
                }
            } + 1

        return TransformUntilStableResult(iterations, currentGrid)
    }

    fun dijkstra(start: Tile<T>, end: Tile<T>, neighbours: (Tile<T>) -> List<Scored<Tile<T>>>) =
        dijkstraPath {
            this.start = start
            this.end = end

            neighbours { neighbours(it) }
        }

    fun hasSameTiles(other: Grid<T>) =
        this.tiles.map { Pair(it.point, it.data) } == other.tiles.map { Pair(it.point, it.data) }

    private fun wrapAround(point: Point2D): Point2D {
        val newX = if (wrapAroundX) point.x.mod(width) else point.x
        val newY = if (wrapAroundY) point.y.mod(height) else point.y

        return Point2D(newX, newY)
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
    parseAsciiGrid(input.lines(), true, processTile)

fun <T> parseAsciiGrid(
    lines: List<String>,
    trim: Boolean = true,
    processTile: (x: Int, y: Int, char: Char) -> T?,
): List<T> {
    val rows =
        if (trim) {
            lines.filterNotBlank()
        } else {
            lines
        }
    return rows.flatMapIndexed { y, row ->
        row.toCharArray().mapIndexed { x, char -> processTile(x, y, char) }.filterNotNull()
    }
}

fun parseAsciiPointGrid(
    lines: List<String>,
    pointMarker: Char = '#',
): List<Point2D> =
    parseAsciiGrid(lines) { x, y, char -> if (char == pointMarker) Point2D(x, y) else null }

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
