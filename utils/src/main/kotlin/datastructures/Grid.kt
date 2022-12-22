package datastructures

import utils.Scored
import utils.filterNotBlank
import utils.genericDijkstra
import utils.toRangeBy

data class Tile<T>(val point: Point2D, val data: T) {
    lateinit var grid: Grid<T>

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

class Grid<T>(tiles: List<Tile<T>>) {
    private val _tiles: MutableMap<Point2D, Tile<T>>

    init {
        _tiles = tiles.associateBy { it.point }.toMutableMap()
        _tiles.values.onEach { it.grid = this }
    }

    val tiles: Collection<Tile<T>>
        get() = _tiles.values

    val width: Int
        get() = _tiles.values.maxOf { it.point.x } + 1
    val height: Int
        get() = _tiles.values.maxOf { it.point.y } + 1

    fun rowBounds(row: Int) = tiles.filter { it.point.y == row }.toRangeBy { it.point.x }

    fun columnBounds(column: Int) = tiles.filter { it.point.x == column }.toRangeBy { it.point.y }

    fun tileAt(point: Point2D) = _tiles[point]

    fun addTile(point: Point2D, data: T) {
        val tile = Tile(point, data)
        tile.grid = this
        this._tiles[point] = tile
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

fun <T> parseGrid(input: String, tileData: (char: Char) -> T): Grid<T> {
    val tiles =
        input.lines().filterNotBlank().flatMapIndexed { y, row ->
            row.toCharArray().mapIndexed { x, char -> Tile(Point2D(x, y), tileData(char)) }
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
