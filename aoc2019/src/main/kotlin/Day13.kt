import ArcadeTile.BALL
import ArcadeTile.PADDLE
import datastructures.Point2D
import intcode.IntcodeInterpreter
import intcode.InterpreterStatus.WAITING_FOR_INPUT
import intcode.parseIntcodeProgram
import utils.ordinalEnumValueOf
import utils.part1
import utils.part2
import utils.readResourceAsString

private enum class ArcadeTile {
    EMPTY,
    WALL,
    BLOCK,
    PADDLE,
    BALL
}

private data class ProgramPixel(val position: Point2D, val tile: ArcadeTile)

private fun parseProgramPixel(input: List<Long>) =
    input.let { (x, y, tileId) ->
        val position = Point2D(x.toInt(), y.toInt())
        val tile =
            ordinalEnumValueOf<ArcadeTile>(tileId.toInt()) ?: error("Unknown tile id: $tileId")

        ProgramPixel(position, tile)
    }

private class Screen {
    var score: Long = 0
    val pixels = mutableMapOf<Point2D, ArcadeTile>()

    fun update(data: List<Long>) {
        val groups = data.windowed(3, 3)
        val updatedScore = groups.find { (x, y) -> x == -1L && y == 0L }?.last()
        if (updatedScore != null) {
            score = updatedScore
        }

        val changedPixels =
            groups
                .filterNot { (x, y) -> x == -1L && y == 0L }
                .map { parseProgramPixel(it) }
                .map { it.position to it.tile }

        pixels.putAll(changedPixels)
    }

    fun paddlePosition() = pixels.entries.find { it.value == PADDLE }!!.key
    fun ballPosition() = pixels.entries.find { it.value == BALL }!!.key
}

fun main() {
    val input = readResourceAsString("/day13.txt")
    val program = parseIntcodeProgram(input)

    part1 {
        val interpreter = IntcodeInterpreter(program)
        interpreter.run()
        interpreter.fullOutput
            .windowed(3, 3)
            .map { parseProgramPixel(it) }
            .count { it.tile == ArcadeTile.BLOCK }
    }
    part2 {
        val screen = Screen()
        val interpreter = IntcodeInterpreter(program)
        interpreter.poke(0, 2)

        fun gameTick() {
            interpreter.run()
            screen.update(interpreter.fullOutput)
            interpreter.clearOutput()
        }

        val ballPositions = mutableListOf<Point2D>()
        while (true) {
            gameTick()
            screen.update(interpreter.fullOutput)
            interpreter.clearOutput()

            if (interpreter.status != WAITING_FOR_INPUT) {
                break
            }

            val ball = screen.ballPosition()
            val paddle = screen.paddlePosition()
            ballPositions += ball

            if (ballPositions.size <= 2) {
                continue
            }

            val (b1, b2) = ballPositions.takeLast(2)
            val ballXVelocity = b2.x - b1.x
            val paddleYDistance = paddle.y - ball.y
            val estimatedX = ball.x + ballXVelocity * (paddleYDistance - 1)

            val joystick =
                when {
                    paddle.x > estimatedX -> -1
                    paddle.x < estimatedX -> 1
                    else -> 0
                }

            interpreter.addInput(joystick.toLong())
        }

        check(screen.pixels.entries.none { it.value == ArcadeTile.BLOCK }) {
            "Should have cleared the game"
        }

        screen.score
    }
}
