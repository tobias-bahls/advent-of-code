import Day11Tile.Galaxy
import datastructures.Point2D
import datastructures.parseGrid
import utils.*

private sealed interface Day11Tile {
    data object Space : Day11Tile

    data class Galaxy(val id: String) : Day11Tile
}

private fun solve(input: String, spaceSize: Long): Long {
    var galaxyCounter = 1
    val grid =
        parseGrid(input) {
            when (it) {
                '.' -> Day11Tile.Space
                '#' -> Galaxy(galaxyCounter++.toString())
                else -> unreachable("char $it")
            }
        }

    val emptyRows =
        (0 until grid.height)
            .filter { rowNum -> grid.row(rowNum).all { it.data == Day11Tile.Space } }
            .toSet()
    val emptyCols =
        (0 until grid.width)
            .filter { colNum -> grid.column(colNum).all { it.data == Day11Tile.Space } }
            .toSet()

    val stars = grid.tiles.filter { it.data is Galaxy }
    val pairs =
        stars.flatMap { g1 -> (stars.dropWhile { it != g1 }.drop(1)).map { g2 -> g1 to g2 } }
    return pairs.sumOf { (a, b) ->
        val left =
            Point2D(
                a.point.x.coerceAtMost(b.point.x),
                a.point.y.coerceAtMost(b.point.y),
            )
        val right =
            Point2D(
                a.point.x.coerceAtLeast(b.point.x),
                a.point.y.coerceAtLeast(b.point.y),
            )

        val xOffset = (emptyCols intersect (left.x..right.x).toSet()).size
        val yOffset = (emptyRows intersect (left.y..right.y).toSet()).size

        val xDist = (right.x - left.x) + xOffset * spaceSize
        val yDist = (right.y - left.y) + yOffset * spaceSize

        (xDist + yDist)
    }
}

fun main() {
    part1 {
        val input = readResourceAsString("/day11.txt")
        solve(input, 1)
    }

    part2 {
        val input = readResourceAsString("/day11.txt")
        solve(input, 1000000 - 1)
    }
}
