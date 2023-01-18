import datastructures.CardinalDirection
import datastructures.CardinalDirection.CardinalDirectionOrthogonal
import datastructures.Point2D
import datastructures.movementVector
import utils.mapFirst
import utils.mapSecond
import utils.match
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toPair

private data class WireSegment(val direction: CardinalDirectionOrthogonal, val steps: Int)

private fun parseDay03Instruction(input: String): WireSegment {
    val (dir, steps) =
        input
            .match("""([RUDL])(\d+)""")
            .toPair()
            .mapFirst {
                when (it) {
                    "U" -> CardinalDirection.North
                    "R" -> CardinalDirection.East
                    "D" -> CardinalDirection.South
                    "L" -> CardinalDirection.West
                    else -> error("Unknown direction: $it")
                }
            }
            .mapSecond { it.toInt() }

    return WireSegment(dir, steps)
}

private fun parseWire(input: String): List<WireSegment> =
    input.trim().split(",").map { parseDay03Instruction(it) }

private fun getWirePoints(instructions: List<WireSegment>): List<Point2D> {
    return instructions.fold(listOf(Point2D.ZERO)) { positions, ins ->
        val currentPosition = positions.last()
        val newPoints = (1..ins.steps).map { currentPosition + (ins.direction.movementVector * it) }

        positions + newPoints
    }
}

private fun findWireIntersections(
    first: List<WireSegment>,
    second: List<WireSegment>
): Set<Point2D> {
    val pointsOnFirst = getWirePoints(first).toSet()
    val pointsOnSecond = getWirePoints(second).toSet()

    return pointsOnFirst.intersect(pointsOnSecond) - Point2D.ZERO
}

fun main() {
    val sample =
        """
        R8,U5,L5,D3
        U7,R6,D4,L4
    """
            .trimIndent()
    val input = readResourceAsString("/day03.txt")
    val (first, second) = input.parseLines { parseWire(it) }

    part1 {
        val intersections = findWireIntersections(first, second)
        intersections.minOf { it.manhattanDistanceTo(Point2D.ZERO) }
    }
    part2 {
        val intersections = findWireIntersections(first, second)

        val firstWirePoints = getWirePoints(first)
        val secondWirePoints = getWirePoints(second)

        intersections.minOf { firstWirePoints.indexOf(it) + secondWirePoints.indexOf(it) }
    }
}
