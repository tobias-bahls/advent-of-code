import datastructures.*
import datastructures.CardinalDirection.CardinalDirectionOrthogonal
import datastructures.CardinalDirection.North
import datastructures.LeftRightDirection.Right
import utils.*

data class Day06Guard(
    val position: Point2D,
    val direction: CardinalDirectionOrthogonal,
) {
    fun turnRight() = copy(direction = direction.turn(Right))

    fun moveForward() = copy(position = position + direction.movementVector)
}

fun hasLoop(startPosition: Point2D, grid: Grid<Boolean>): Boolean {
    var guard = Day06Guard(startPosition, North)
    val seenGuardStates = mutableSetOf<Day06Guard>()
    var loop = false
    repeatUntilTrue {
        if (guard in seenGuardStates) {
            loop = true
            return@repeatUntilTrue true
        }
        seenGuardStates.add(guard)

        val guardTile = grid.tileAt(guard.position) ?: error("Guard outside of grid")
        val nextTile =
            guardTile.directNeighbourInDirection(guard.direction) ?: return@repeatUntilTrue true
        if (nextTile.data) {
            guard = guard.turnRight()
        } else {
            guard = guard.moveForward()
        }

        false
    }

    return loop
}

fun main() {
    part1 {
        val input = readResourceAsString("/day06.txt")
        val grid = parseGrid(input) { it == '#' }
        val startPosition = parseGrid(input) { it == '^' }.tiles.single { it.data }.point

        var guard = Day06Guard(startPosition, North)
        val visitedPoints = mutableSetOf<Point2D>()
        repeatUntilTrue {
            visitedPoints.add(guard.position)

            val guardTile = grid.tileAt(guard.position) ?: error("Guard outside of grid")
            val nextTile =
                guardTile.directNeighbourInDirection(guard.direction) ?: return@repeatUntilTrue true
            if (nextTile.data) {
                guard = guard.turnRight()
            } else {
                guard = guard.moveForward()
            }

            false
        }

        visitedPoints.size
    }

    part2 {
        val input = readResourceAsString("/day06.txt")
        val startPosition = parseGrid(input) { it == '^' }.tiles.single { it.data }.point
        val grid = parseGrid(input) { it == '#' }

        grid.yRangeProgression.sumOf { y ->
            grid.xRangeProgression.count { x ->
                val obstacle = Point2D(x, y)
                if (obstacle == startPosition) {
                    return@count false
                }
                val obstacleGrid = parseGrid(input) { it == '#' }
                obstacleGrid.addTile(obstacle, true)
                hasLoop(startPosition, obstacleGrid)
            }
        }
    }
}
