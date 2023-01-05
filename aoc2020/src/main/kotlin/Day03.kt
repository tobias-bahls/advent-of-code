import datastructures.Grid
import datastructures.Point2D
import datastructures.parseGrid
import utils.part1
import utils.part2
import utils.readResourceAsString

private fun countTrees(grid: Grid<Boolean>, slope: Point2D): Int {
    var current = Point2D(0, 0)
    var trees = 0

    while (current.y < grid.height) {
        current += slope

        if (grid.tileAt(current)?.data == true) {
            trees++
        }
    }

    return trees
}

fun main() {
    val input = readResourceAsString("/day03.txt")
    val grid = parseGrid(input) { it == '#' }
    grid.wrapAroundX = true

    part1 { countTrees(grid, Point2D(3, 1)) }

    part2 {
        listOf(Point2D(1, 1), Point2D(3, 1), Point2D(5, 1), Point2D(7, 1), Point2D(1, 2))
            .map { countTrees(grid, it).toLong() }
            .reduce(Long::times)
    }
}
