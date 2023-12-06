import RepairDroidTile.FLOOR
import RepairDroidTile.TARGET
import RepairDroidTile.WALL
import algorithms.dijkstraPath
import datastructures.CardinalDirection.CardinalDirectionOrthogonal
import datastructures.CardinalDirection.East
import datastructures.CardinalDirection.North
import datastructures.CardinalDirection.South
import datastructures.CardinalDirection.West
import datastructures.Point2D
import datastructures.movementVector
import intcode.IntcodeInterpreter
import intcode.parseIntcodeProgram
import utils.QueueMode
import utils.Scored
import utils.part1
import utils.part2
import utils.queue
import utils.readResourceAsString
import utils.repeatUntilTrue

private fun IntcodeInterpreter.move(dir: CardinalDirectionOrthogonal) {
    val input =
        when (dir) {
            North -> 1
            South -> 2
            West -> 3
            East -> 4
        }

    addInput(input.toLong())
    run()
}

private fun IntcodeInterpreter.lastDetectedTile() =
    when (lastOutput) {
        0L -> WALL
        1L -> FLOOR
        2L -> TARGET
        else -> error("Unknown output: $lastOutput")
    }

private enum class RepairDroidTile {
    FLOOR,
    WALL,
    TARGET
}

private fun pathTo(
    current: Point2D,
    dest: Point2D,
    discoveredPoints: Map<Point2D, RepairDroidTile>
): List<CardinalDirectionOrthogonal> {
    val path =
        dijkstraPath<Point2D> {
            start = current
            end = dest

            neighbours { current ->
                current.neighboursOrthogonally
                    .filter {
                        it == dest ||
                            discoveredPoints[it] == FLOOR ||
                            discoveredPoints[it] == TARGET
                    }
                    .map { Scored(1, it) }
            }
        } ?: error("No path from $current to $dest")

    return path.windowed(2).map { (a, b) ->
        CardinalDirectionOrthogonal.ALL.find { b - a == it.movementVector }
            ?: error("No movement vector from $a to $b")
    }
}

private fun reconstructMap(program: List<Long>): Map<Point2D, RepairDroidTile> {
    val robot = IntcodeInterpreter(program)

    var currentPosition = Point2D.ZERO
    fun tryMove(dir: CardinalDirectionOrthogonal): RepairDroidTile {
        robot.move(dir)
        val output = robot.lastDetectedTile()

        if (output != WALL) {
            currentPosition += dir.movementVector
        }

        return output
    }

    val discoveredPoints = mutableMapOf(currentPosition to FLOOR)
    queue<Point2D>(currentPosition.neighboursOrthogonally, QueueMode.LIFO) { target ->
        if (target in discoveredPoints) return@queue skip()

        val movement = pathTo(currentPosition, target, discoveredPoints)

        movement.dropLast(1).forEach {
            val output = tryMove(it)
            check(output != WALL) {
                "Movement $it from ${currentPosition - it.movementVector} should be possible, but wasn't"
            }
        }

        val discoveredTile = tryMove(movement.last())
        discoveredPoints[target] = discoveredTile

        if (discoveredTile == WALL) {
            skip()
        } else {
            enqueue(currentPosition.neighboursOrthogonally)
        }
    }

    return discoveredPoints
}

fun main() {
    val input = readResourceAsString("/day15.txt")
    val program = parseIntcodeProgram(input)

    part1 {
        val discoveredPoints = reconstructMap(program)
        val target =
            discoveredPoints.entries.find { it.value == TARGET }?.key
                ?: error("Could not find target")

        pathTo(Point2D.ZERO, target, discoveredPoints).size
    }
    part2 {
        val discoveredPoints = reconstructMap(program)
        val targetTile =
            discoveredPoints.entries.find { it.value == TARGET }?.key
                ?: error("Could not find target")

        val vacuumTiles =
            discoveredPoints.entries.filter { it.value == FLOOR }.map { it.key }.toMutableSet()
        val oxygenTiles = mutableSetOf(targetTile)

        repeatUntilTrue {
            val newOxygenTiles =
                oxygenTiles
                    .flatMap { it.neighboursOrthogonally }
                    .filter { it in vacuumTiles }
                    .toSet()

            vacuumTiles -= newOxygenTiles
            oxygenTiles += newOxygenTiles

            vacuumTiles.isEmpty()
        } + 1
    }
}
