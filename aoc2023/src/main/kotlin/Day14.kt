import Day14Tile.*
import datastructures.CardinalDirection.*
import datastructures.Grid
import datastructures.Point2D
import datastructures.parseGrid
import utils.*

private enum class Day14Tile {
    FLOOR,
    ROUND_ROCK,
    CUBE_ROCK
}

private fun tiltStep(
    grid: Grid<Day14Tile>,
    direction: CardinalDirectionOrthogonal
): Grid<Day14Tile> {
    return Grid(
        grid.tiles.map {
            when (it.data) {
                FLOOR -> {
                    val neighbourInOppositeDirection =
                        it.directNeighbourInDirection(direction.opposite())
                    val willBeMovedOn =
                        neighbourInOppositeDirection != null &&
                            neighbourInOppositeDirection.data == ROUND_ROCK

                    if (willBeMovedOn) {
                        it.copy(data = ROUND_ROCK)
                    } else {
                        it
                    }
                }
                ROUND_ROCK -> {
                    val neighbourInTiltDirection = it.directNeighbourInDirection(direction)
                    val canMove =
                        neighbourInTiltDirection != null && neighbourInTiltDirection.data == FLOOR

                    if (canMove) {
                        it.copy(data = FLOOR)
                    } else {
                        it
                    }
                }
                CUBE_ROCK -> it
            }
        })
}

private fun calculateLoadOnNorthSupportBeam(grid: Grid<Day14Tile>) =
    grid.tiles.filter { it.data == ROUND_ROCK }.sumOf { grid.height - it.point.y }

private fun parseDay14Grid(str: String) =
    parseGrid(str) {
        when (it) {
            'O' -> ROUND_ROCK
            '#' -> CUBE_ROCK
            '.' -> FLOOR
            else -> unreachable("$it")
        }
    }

fun main() {
    part1 {
        val input = readResourceAsString("/day14.txt")
        val grid = parseDay14Grid(input)

        val tilted = grid.transformUntilStable { tiltStep(it, North) }.resultGrid

        calculateLoadOnNorthSupportBeam(tilted)
    }

    part2 {
        val input = readResourceAsString("/day14.txt")
        val startGrid = parseDay14Grid(input)

        fun oneTurn(grid: Grid<Day14Tile>): Grid<Day14Tile> {
            val tiltDirs = listOf(North, West, South, East)

            return tiltDirs.fold(grid) { g, dir ->
                g.transformUntilStable { tiltStep(it, dir) }.resultGrid
            }
        }

        data class Fingerprint(val points: Set<Point2D>)
        fun fingerprintGrid(grid: Grid<Day14Tile>): Fingerprint {
            return Fingerprint(grid.tiles.filter { it.data == ROUND_ROCK }.map { it.point }.toSet())
        }

        val seen = mutableListOf(fingerprintGrid(startGrid))
        var loopStart = 0
        var loopSize = 0
        algorithms
            .repeat(startGrid) {
                val turned = oneTurn(it)
                val fingerprint = fingerprintGrid(turned)
                val l = seen.indexOfFirst { it == fingerprint }
                if (l != -1) {
                    loopStart = l - 1
                    loopSize = currentIteration - l + 1
                    return@repeat stop()
                }

                seen.add(fingerprint)
                next(turned)
            }
            .iterations

        val totalCycles = 1_000_000_000
        val idx = (totalCycles - loopStart) % loopSize

        seen[loopStart + idx].points.sumOf { startGrid.height - it.y }
    }
}
