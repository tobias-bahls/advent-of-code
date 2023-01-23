import HullColor.BLACK
import HullColor.WHITE
import datastructures.CardinalDirection.CardinalDirectionOrthogonal
import datastructures.CardinalDirection.North
import datastructures.LeftRightDirection
import datastructures.Point2D
import datastructures.movementVector
import intcode.IntcodeInterpreter
import intcode.InterpreterStatus
import intcode.parseIntcodeProgram
import utils.BLOCK
import utils.mapFirst
import utils.mapSecond
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toPair
import utils.toRangeBy
import utils.wrap

private enum class HullColor {
    BLACK,
    WHITE
}

private fun parseHullColor(value: Long) =
    when (value) {
        0L -> BLACK
        1L -> WHITE
        else -> error("Unknown hull color: $value")
    }

private fun hullColorToInput(value: HullColor) =
    when (value) {
        BLACK -> 0L
        WHITE -> 1L
    }

private data class PaintingRobot(
    val location: Point2D,
    val direction: CardinalDirectionOrthogonal
) {
    fun turnAndMove(dir: LeftRightDirection) =
        copy(
            direction = direction.turn(dir),
            location = location + direction.turn(dir).movementVector)
}

private data class Hull(val colors: Map<Point2D, HullColor>) {
    fun at(point: Point2D) = colors[point] ?: BLACK

    fun paint(point: Point2D, color: HullColor) = copy(colors = colors + (point to color))
}

private fun runHullRobot(
    program: List<Long>,
    startingHull: Hull,
    startingRobot: PaintingRobot
): Hull {
    val robotProgram = IntcodeInterpreter(program)

    var hull = startingHull
    var robot = startingRobot
    while (robotProgram.status != InterpreterStatus.HALTED) {
        robotProgram.addInput(hullColorToInput(hull.at(robot.location)))
        robotProgram.run()

        val (paintColor, turn) =
            robotProgram.output
                .takeLast(2)
                .toPair()
                .mapFirst { parseHullColor(it) }
                .mapSecond { parseRobotDirection(it) }

        hull = hull.paint(robot.location, paintColor)
        robot = robot.turnAndMove(turn)
    }

    return hull
}

private fun parseRobotDirection(it: Long): LeftRightDirection =
    when (it) {
        0L -> LeftRightDirection.Left
        1L -> LeftRightDirection.Right
        else -> error("Unknown direction: $it")
    }

fun main() {
    val input = readResourceAsString("/day11.txt")
    val program = parseIntcodeProgram(input)

    part1 {
        val robot = PaintingRobot(Point2D.ZERO, North)
        val hull = Hull(emptyMap())

        runHullRobot(program, hull, robot).colors.size
    }
    part2 {
        val robot = PaintingRobot(Point2D.ZERO, North)
        val hull = Hull(mapOf(robot.location to WHITE))

        val paintedHull = runHullRobot(program, hull, robot)

        val xRange = paintedHull.colors.keys.toRangeBy { it.x }
        val yRange = paintedHull.colors.keys.toRangeBy { it.y }

        (yRange.start..yRange.endInclusive)
            .joinToString("\n") { y ->
                (xRange.start..xRange.endInclusive).joinToString("") { x ->
                    if (paintedHull.at(Point2D(x, y)) == WHITE) BLOCK.toString() else " "
                }
            }
            .wrap("\n")
    }
}
