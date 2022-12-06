package utils

data class Tile<T>(val grid: Grid<T>, val point: Point, val data: T) {
    fun adjacentOrthogonally(): List<Tile<T>> =
        point.neighboursOrthogonally.mapNotNull { grid.tileAt(it) }
    fun adjacent(): List<Tile<T>> = point.neighbours.mapNotNull { grid.tileAt(it) }

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

class Grid<T>(input: String, createTile: (Char) -> T) {
    val tiles: List<Tile<T>>

    init {
        val tiles =
            input.lines().filterNotBlank().flatMapIndexed { y, row ->
                row.toCharArray().mapIndexed { x, char ->
                    Tile(this, Point(x, y), createTile(char))
                }
            }

        this.tiles = tiles
    }

    fun tileAt(point: Point) = tiles.firstOrNull { it.point == point }

    override fun toString(): String {
        return "Grid(tiles=$tiles)"
    }
}
