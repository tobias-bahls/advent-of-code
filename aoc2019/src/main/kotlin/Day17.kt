import datastructures.CardinalDirection
import datastructures.CardinalDirection.CardinalDirectionOrthogonal
import datastructures.LeftRightDirection
import datastructures.LeftRightDirection.Left
import datastructures.LeftRightDirection.Right
import datastructures.Point2D
import datastructures.movementVector
import datastructures.parseAsciiGrid
import datastructures.parseGrid
import intcode.IntcodeInterpreter
import intcode.interact
import intcode.parseIntcodeProgram
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.regexParts

private fun getAsciiMap(program: List<Long>): String {
    val interpreter = IntcodeInterpreter(program)

    interpreter.run()

    return interpreter.fullOutput.map { it.toInt().toChar() }.joinToString("")
}

private data class Robot(
    val position: Point2D,
    val direction: CardinalDirectionOrthogonal,
) {

    val nextPoint = position + direction.movementVector

    fun turn(dir: LeftRightDirection) = copy(direction = direction.turn(dir))

    fun forward() = copy(position = nextPoint)
}

private fun findInitialRobot(map: String) =
    parseAsciiGrid(map) { x, y, char ->
            val dir =
                when (char) {
                    '>' -> CardinalDirection.East
                    'v' -> CardinalDirection.South
                    '<' -> CardinalDirection.West
                    '^' -> CardinalDirection.North
                    else -> return@parseAsciiGrid null
                }
            Robot(Point2D(x, y), dir)
        }
        .singleOrNull()!!

private fun findPathToEnd(program: List<Long>): String {
    val map = getAsciiMap(program)

    val initialRobot = findInitialRobot(map)
    val grid = parseGrid(map) { it == '#' || it in listOf('<', '>', '^', 'v') }

    var result = ""
    fun visitable(point: Point2D) = grid.tileAt(point)?.data == true

    var robot = initialRobot
    while (true) {
        if (!visitable(robot.nextPoint)) {
            val turnedLeft = robot.turn(Left)
            val turnedRight = robot.turn(Right)
            when {
                visitable(turnedLeft.nextPoint) -> {
                    robot = turnedLeft
                    result += "L"
                }
                visitable(turnedRight.nextPoint) -> {
                    robot = turnedRight
                    result += "R"
                }
                else -> break
            }
        }

        var moves = 0
        while (visitable(robot.nextPoint)) {
            moves++
            robot = robot.forward()
        }

        result += moves
    }

    return result
}

fun main() {
    val input = readResourceAsString("/day17.txt")
    val program = parseIntcodeProgram(input)

    part1 {
        parseGrid(getAsciiMap(program)) { it == '#' }
            .tiles
            .filter { it.adjacentOrthogonally().all { n -> n.data } }
            .sumOf { it.point.x * it.point.y }
    }
    part2 {
        val fullPath = findPathToEnd(program)

        fun compressPath(input: List<String>, depth: Int = 0): List<String>? {
            if (input.isEmpty()) {
                return emptyList()
            }
            if (depth == 3) {
                return null
            }

            for (i in 1..input[0].length) {
                val candidate = input[0].substring(0, i)
                if (candidate.length - 1 > 20) {
                    break
                }

                val fragments = input.flatMap { it.split(candidate) }.filter { it.isNotEmpty() }
                val res = compressPath(fragments, depth + 1)
                if (res != null) {
                    return listOf(candidate) + res
                }
            }
            return null
        }

        val funs = compressPath(listOf(fullPath)) ?: error("No solution")
        val namedFuns = listOf("A", "B", "C").zip(funs).toMap()

        val mainRoutine =
            namedFuns.entries
                .fold(fullPath) { path, (name, value) -> path.replace(value, name) }
                .toList()
                .joinToString(",")

        val solution =
            namedFuns.mapValues { (_, v) -> v.regexParts("""(\d+|L|R)""").joinToString(",") }

        val interpreter = IntcodeInterpreter(program)
        interpreter.poke(0, 2L)
        interpreter.run()
        interpreter.readOutput() // Skip over initial display of map

        interpreter.interact {
            writeLine(mainRoutine)

            expectAsciiOutput("Function A:\n")
            writeLine(solution.getValue("A"))

            expectAsciiOutput("Function B:\n")
            writeLine(solution.getValue("B"))

            expectAsciiOutput("Function C:\n")
            writeLine(solution.getValue("C"))

            expectAsciiOutput("Continuous video feed?\n")
            writeLine("n")
        }

        interpreter.lastOutput
    }
}
