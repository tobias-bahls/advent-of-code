package datastructures

import utils.Scored
import utils.genericDijkstra
import utils.map
import utils.parseGrid

data class Tile<T>(val grid: Grid<T>, val point: Point2D, val data: T) {
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

class Grid<T>(input: String, createTile: (Char) -> T) {
    private val _tiles: MutableMap<Point2D, Tile<T>>

    val tiles: Collection<Tile<T>>
        get() = _tiles.values

    val width: Int
        get() = _tiles.values.maxOf { it.point.x } + 1
    val height: Int
        get() = _tiles.values.maxOf { it.point.y } + 1

    init {
        this._tiles =
            parseGrid(input) { x, y, char -> Tile(this, Point2D(x, y), createTile(char)) }
                .associateBy { it.point }
                .toMutableMap()
    }

    fun tileAt(point: Point2D) = _tiles[point]

    fun addTile(point: Point2D, data: T) {
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

class SparseGrid(val points: Set<Point2D>) {
    val width: Int = points.maxOf { it.x }
    val height: Int = points.maxOf { it.y }

    fun hasPoint(x: Int, y: Int): Boolean = points.contains(Point2D(x, y))
}
