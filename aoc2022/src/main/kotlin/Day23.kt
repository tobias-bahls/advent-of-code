import datastructures.CardinalDirection.*
import datastructures.Grid
import datastructures.Tile
import datastructures.parseGrid
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.reduceTimes
import utils.untilTrue

private data class Elf(
    val directionList: List<CardinalDirectionOrthogonal> = listOf(North, South, West, East)
) {
    fun cycleDirections() = copy(directionList = directionList.drop(1) + directionList.take(1))
}

private fun step(grid: Grid<Elf>): Grid<Elf> {
    val targetsToElves =
        grid.tiles.groupBy { elf ->
            val needsToMove = elf.adjacent().isNotEmpty()

            if (!needsToMove) {
                return@groupBy null
            }

            val moveDirection =
                elf.data.directionList.find { dir ->
                    val lookAt =
                        when (dir) {
                            North -> listOf(North, NorthEast, NorthWest)
                            South -> listOf(South, SouthEast, SouthWest)
                            West -> listOf(West, NorthWest, SouthWest)
                            East -> listOf(East, NorthEast, SouthEast)
                        }

                    elf.directNeighboursInDirections(lookAt).isEmpty()
                }
                    ?: return@groupBy null

            elf.pointInCardinalDirection(moveDirection)
        }

    val newElves =
        targetsToElves.flatMap { (point, elves) ->
            if (point == null) {
                elves.map { Tile(it.point, it.data.cycleDirections()) }
            } else {
                if (elves.size == 1) {
                    listOf(Tile(point, elves.single().data.cycleDirections()))
                } else {
                    elves.map { Tile(it.point, it.data.cycleDirections()) }
                }
            }
        }

    return Grid(newElves)
}

fun main() {
    val input = readResourceAsString("/day23.txt")
    val grid =
        parseGrid(input) {
            when (it) {
                '#' -> Elf()
                '.' -> null
                else -> error("Unknown tile: $it")
            }
        }

    part1 {
        val resultGrid = reduceTimes(10, grid, ::step)

        resultGrid.width * resultGrid.height - resultGrid.tiles.size
    }

    part2 {
        var currentGrid = grid
        untilTrue {
            val newGrid = step(currentGrid)

            if (newGrid.tiles.map { it.point } == currentGrid.tiles.map { it.point }) {
                return@untilTrue true
            }

            currentGrid = newGrid
            false
        } + 1
    }
}
