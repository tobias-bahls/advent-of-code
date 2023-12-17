import algorithms.dijkstraPath
import datastructures.*
import datastructures.CardinalDirection.CardinalDirectionOrthogonal
import datastructures.CardinalDirection.East
import datastructures.LeftRightDirection.Left
import datastructures.LeftRightDirection.Right
import utils.*

private fun solve(grid: Grid<Int>, maxStraight: Int, minMovesSinceTurn: Int): Int {
    data class State(
        val position: Point2D,
        val straightRemaining: Int,
        val facing: CardinalDirectionOrthogonal,
        val movesSinceTurn: Int
    )

    val initialState = State(Point2D.ZERO, maxStraight, East, 0)
    val goal = Point2D(grid.width - 1, grid.height - 1)

    val path =
        dijkstraPath<State> {
            start = initialState
            endCondition { it.position == goal }

            neighbours { state ->
                val straightNeighbour =
                    if (state.straightRemaining > 0) {
                        val newPos = state.position + state.facing.movementVector
                        val tile = grid.tileAt(newPos)
                        if (tile == null) {
                            null
                        } else {
                            state
                                .copy(
                                    position = newPos,
                                    straightRemaining = state.straightRemaining - 1,
                                    movesSinceTurn = state.movesSinceTurn + 1,
                                )
                                .scored(tile.data)
                        }
                    } else {
                        null
                    }

                val turnNeighbours =
                    if (state.movesSinceTurn >= minMovesSinceTurn) {
                        listOf(
                                state.copy(
                                    facing = state.facing.turn(Left),
                                    straightRemaining = maxStraight,
                                    movesSinceTurn = 0),
                                state.copy(
                                    facing = state.facing.turn(Right),
                                    straightRemaining = maxStraight,
                                    movesSinceTurn = 0),
                            )
                            .filter { grid.tileAt(it.position + it.facing.movementVector) != null }
                            .map { it.scored(0) }
                    } else {
                        emptyList()
                    }

                (listOfNotNull(straightNeighbour) + turnNeighbours)
            }
        }

    return path.orEmpty().drop(1).map { it.position }.distinct().sumOf { grid.tileAt(it)!!.data }
}

fun main() {
    part1 {
        val input = readResourceAsString("/day17.txt")
        val grid = parseGrid(input) { it.digitToInt() }

        solve(grid, maxStraight = 3, minMovesSinceTurn = 1)
    }

    part2 {
        val input = readResourceAsString("/day17.txt")
        val grid = parseGrid(input) { it.digitToInt() }

        solve(grid, maxStraight = 10, minMovesSinceTurn = 4)
    }
}
