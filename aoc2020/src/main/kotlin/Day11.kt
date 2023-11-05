import datastructures.CardinalDirection
import datastructures.Grid
import datastructures.parseGrid
import utils.part1
import utils.part2
import utils.readResourceAsString

private fun stepPart1(grid: Grid<Boolean>): Grid<Boolean> {
    return grid.tiles
        .map { tile ->
            val occupiedNeighbours = tile.adjacent().count { it.data }

            when {
                tile.data && occupiedNeighbours >= 4 -> tile.copy(data = false)
                !tile.data && occupiedNeighbours == 0 -> tile.copy(data = true)
                else -> tile
            }
        }
        .let { Grid(it) }
}

private fun stepPart2(grid: Grid<Boolean>): Grid<Boolean> {
    return grid.tiles
        .map { tile ->
            val occupiedNeighbours =
                CardinalDirection.ALL.mapNotNull { direction ->
                        tile.allTilesInDirection(direction).firstOrNull()
                    }
                    .count { it.data }

            when {
                tile.data && occupiedNeighbours >= 5 -> tile.copy(data = false)
                !tile.data && occupiedNeighbours == 0 -> tile.copy(data = true)
                else -> tile
            }
        }
        .let { Grid(it) }
}

fun main() {
    val input = readResourceAsString("/day11.txt")
    val grid =
        parseGrid(input) {
            when (it) {
                '.' -> null
                'L' -> false
                else -> error("Unknown char: $it")
            }
        }

    part1 { grid.transformUntilStable { stepPart1(it) }.resultGrid.tiles.count { it.data } }
    part2 { grid.transformUntilStable { stepPart2(it) }.resultGrid.tiles.count { it.data } }
}
