package `2021`

import filterNotBlank
import part1
import part2
import readResourceAsString
import visitAllNodes

data class Point(val x: Int, val y: Int) {

    fun left() = Point(x + 1, y)
    fun right() = Point(x - 1, y)
    fun top() = Point(x, y - 1)
    fun bottom() = Point(x, y + 1)

    fun neighbours() = listOf(left(), right(), top(), bottom())
}

data class Tile(val point: Point, val height: Int, val map: Heightmap) {
    fun adjacent(): List<Tile> = point.neighbours().mapNotNull { map.pointAt(it) }
    override fun toString(): String {
        return "Tile(point=$point, height=$height)"
    }
}

class Heightmap(input: String) {
    val tiles: List<Tile>

    init {
        val tiles =
            input.lines().filterNotBlank().flatMapIndexed { y, row ->
                row.toCharArray().mapIndexed { x, char ->
                    Tile(Point(x, y), char.digitToInt(), this)
                }
            }

        this.tiles = tiles
    }

    fun pointAt(point: Point) = tiles.firstOrNull { it.point == point }

    fun lowPoints(): List<Tile> =
        tiles.filter { tile -> tile.adjacent().all { neighbour -> tile.height < neighbour.height } }

    override fun toString(): String {
        return "Heightmap(tiles=$tiles)"
    }
}

fun main() {
    val input = readResourceAsString("2021/day09.txt")
    val heightmap = Heightmap(input)

    part1 { heightmap.lowPoints().sumOf { it.height + 1 } }

    part2 {
        heightmap
            .lowPoints()
            .map { lowPoint ->
                visitAllNodes(lowPoint) { it.adjacent().filter { adj -> adj.height != 9 } }.size
            }
            .sortedDescending()
            .take(3)
            .reduce(Int::times)
    }
}
