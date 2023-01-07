import ShipInstruction.MoveInCardinalDirection
import datastructures.CardinalDirection.CardinalDirectionOrthogonal
import datastructures.CardinalDirection.East
import datastructures.CardinalDirection.North
import datastructures.CardinalDirection.South
import datastructures.CardinalDirection.West
import datastructures.LeftRightDirection
import datastructures.LeftRightDirection.Left
import datastructures.LeftRightDirection.Right
import datastructures.Point2D
import datastructures.movementVector
import utils.match
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.reduceTimes

private sealed interface ShipInstruction {
    data class MoveInCardinalDirection(val direction: CardinalDirectionOrthogonal, val turns: Int) :
        ShipInstruction
    data class MoveForward(val turns: Int) : ShipInstruction
    data class Turn(val direction: LeftRightDirection, val angle: Int) : ShipInstruction
}

private fun parseInstruction(input: String): ShipInstruction {
    val (char, num) = input.match("""([NSEWLRF])(\d+)""")

    return when (char) {
        "N" -> MoveInCardinalDirection(North, num.toInt())
        "E" -> MoveInCardinalDirection(East, num.toInt())
        "S" -> MoveInCardinalDirection(South, num.toInt())
        "W" -> MoveInCardinalDirection(West, num.toInt())
        "F" -> ShipInstruction.MoveForward(num.toInt())
        "L" -> ShipInstruction.Turn(Left, num.toInt())
        "R" -> ShipInstruction.Turn(Right, num.toInt())
        else -> error("Unknown instruction: $char")
    }
}

private data class Part1Ship(
    val facingDirection: CardinalDirectionOrthogonal,
    val position: Point2D
) {
    fun executeInstruction(ins: ShipInstruction): Part1Ship =
        when (ins) {
            is ShipInstruction.MoveForward ->
                copy(position = position + facingDirection.movementVector * ins.turns)
            is MoveInCardinalDirection ->
                copy(position = position + ins.direction.movementVector * ins.turns)
            is ShipInstruction.Turn ->
                copy(
                    facingDirection =
                        reduceTimes(ins.angle / 90, facingDirection) { it.turn(ins.direction) })
        }
}

private data class Part2Ship(
    val facingDirection: CardinalDirectionOrthogonal,
    val position: Point2D,
    val relativeWaypointPosition: Point2D
) {
    fun executeInstruction(ins: ShipInstruction): Part2Ship =
        when (ins) {
            is ShipInstruction.MoveForward ->
                copy(position = position + (relativeWaypointPosition * ins.turns))
            is MoveInCardinalDirection ->
                copy(
                    relativeWaypointPosition =
                        relativeWaypointPosition + ins.direction.movementVector * ins.turns)
            is ShipInstruction.Turn -> {
                val angle = ins.angle * if (ins.direction == Right) 1 else -1
                copy(relativeWaypointPosition = relativeWaypointPosition.rotate(angle))
            }
        }
}

fun main() {
    val input = readResourceAsString("/day12.txt")
    val instructions = input.parseLines { parseInstruction(it) }

    part1 {
        val startingShip = Part1Ship(East, Point2D(0, 0))
        val finalShip =
            instructions.fold(startingShip) { ship, instruction ->
                ship.executeInstruction(instruction)
            }

        finalShip.position.manhattanDistanceTo(startingShip.position)
    }

    part2 {
        val startingShip = Part2Ship(East, Point2D(0, 0), Point2D(10, -1))
        val finalShip =
            instructions.fold(startingShip) { ship, instruction ->
                ship.executeInstruction(instruction)
            }

        finalShip.position.manhattanDistanceTo(startingShip.position)
    }
}
