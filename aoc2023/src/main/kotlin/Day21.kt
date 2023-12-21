import algorithms.repeat
import datastructures.Grid
import datastructures.Point2D
import datastructures.Tile
import datastructures.parseGrid
import java.math.BigInteger
import utils.*

private enum class Day21Tile {
    START,
    ROCK,
    FLOOR
}

private fun numberOfReachableTiles(
    grid: Grid<Day21Tile>,
    steps: Int,
    infinite: Boolean = false
): Int {
    val startTile =
        grid.tiles.find { it.data == Day21Tile.START } ?: error("could not find start tile")

    data class State(val steps: Int, val tiles: Set<Tile<Day21Tile>>)

    val initial = State(0, setOf(startTile))
    return repeat(initial) { state ->
            if (state.steps == steps) {
                return@repeat stop()
            }

            val nextTiles =
                state.tiles
                    .flatMap { it.point.neighboursOrthogonally }
                    .mapNotNull {
                        val tilePoint =
                            if (infinite) {
                                Point2D(it.x.mod(grid.width), it.y.mod(grid.height))
                            } else {
                                it
                            }
                        grid.tileAt(tilePoint)?.copy(point = it)
                    }
                    .filter { it.data != Day21Tile.ROCK }
                    .toSet()

            next(State(state.steps + 1, nextTiles))
        }
        .element
        .tiles
        .size
}

fun main() {
    part1 {
        val input = readResourceAsString("/day21.txt") // sample

        val grid =
            parseGrid(input) {
                when (it) {
                    'S' -> Day21Tile.START
                    '#' -> Day21Tile.ROCK
                    '.' -> Day21Tile.FLOOR
                    else -> unreachable("$it")
                }
            }

        numberOfReachableTiles(grid, 64)
    }

    part2 {
        val input = readResourceAsString("/day21.txt")

        val grid =
            parseGrid(input) {
                when (it) {
                    'S' -> Day21Tile.START
                    '#' -> Day21Tile.ROCK
                    '.' -> Day21Tile.FLOOR
                    else -> unreachable("$it")
                }
            }

        numberOfReachableTiles(grid, 65 + (grid.width * 0), infinite = true).dump("y0")
        numberOfReachableTiles(grid, 65 + (grid.width * 1), infinite = true).dump("y1")
        numberOfReachableTiles(grid, 65 + (grid.width * 2), infinite = true).dump("y2")

        // Plug into wolfram alpha to calculate quadratic fit
        fun fit(x: BigInteger) =
            15135.toBigInteger() * x.pow(2) + 15251.toBigInteger() * x + 3867.toBigInteger()

        fit(202300.toBigInteger())
    }
}
