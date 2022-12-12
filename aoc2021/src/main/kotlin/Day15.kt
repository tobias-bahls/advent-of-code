import datastructures.Grid
import datastructures.Point
import datastructures.Tile
import utils.part1
import utils.part2
import utils.readResourceAsString

fun enlargeGrid(grid: Grid<Int>) {
    fun calculateNewRisk(tile: Tile<Int>, n: Int): Int =
        (tile.data + n).let {
            if (it < 10) {
                it
            } else {
                it - 9
            }
        }

    val originalWidth = grid.width
    grid.tiles.toList().forEach { tile ->
        (1..4).map { n ->
            val newRisk = calculateNewRisk(tile, n)

            val p1 = Point(tile.point.x + originalWidth * n, tile.point.y)
            grid.addTile(p1, newRisk)
        }
    }

    val originalHeight = grid.height
    grid.tiles.toList().forEach { tile ->
        (1..4).map { n ->
            val newRisk = calculateNewRisk(tile, n)

            val p1 = Point(tile.point.x, tile.point.y + originalHeight * n)
            grid.addTile(p1, newRisk)
        }
    }
}

fun solve(grid: Grid<Int>): Int {
    val start = grid.tileAt(Point(0, 0)) ?: error("Could not find start point")
    val end =
        grid.tileAt(Point(grid.width - 1, grid.height - 1)) ?: error("Could not find end point")

    val dijkstra =
        grid.dijkstra(start, end) { _, neighbour -> neighbour.data } ?: error("No shortest path")

    return dijkstra.drop(1).sumOf { it.data }
}

fun main() {
    val input = readResourceAsString("/day15.txt")

    part1 {
        val grid = Grid(input) { it.digitToInt() }

        solve(grid)
    }

    part2 {
        val grid = Grid(input) { it.digitToInt() }
        enlargeGrid(grid)

        solve(grid)
    }
}
