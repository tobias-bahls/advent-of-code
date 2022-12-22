import CubeFace.*
import FacingDirection.*
import datastructures.Grid
import datastructures.Point2D
import datastructures.parseGridWithEmptyTiles
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.regexParts

private enum class GroveTile {
    WALL,
    FLOOR
}

private sealed interface MonkeyInstruction {
    enum class Turn : MonkeyInstruction {
        LEFT,
        RIGHT
    }

    data class Move(val steps: Int) : MonkeyInstruction
}

private enum class FacingDirection {
    UP,
    RIGHT,
    DOWN,
    LEFT;

    fun turn(turn: MonkeyInstruction.Turn) =
        when (turn) {
            MonkeyInstruction.Turn.LEFT ->
                when (this) {
                    UP -> LEFT
                    RIGHT -> UP
                    DOWN -> RIGHT
                    LEFT -> DOWN
                }
            MonkeyInstruction.Turn.RIGHT ->
                when (this) {
                    UP -> RIGHT
                    RIGHT -> DOWN
                    DOWN -> LEFT
                    LEFT -> UP
                }
        }
}

private data class PositionInGrove(val point: Point2D, val facingDirection: FacingDirection) {
    fun forwardPosition() =
        when (facingDirection) {
            UP -> point.top
            RIGHT -> point.right
            DOWN -> point.bottom
            LEFT -> point.left
        }

    fun cubeFace(faceSize: Int): CubeFace =
        CubeFace.values().find {
            point.x in (it.left(faceSize)..it.right(faceSize)) &&
                point.y in (it.top(faceSize)..it.bottom(faceSize))
        }
            ?: error("Could not determine face for $point")

    fun score() = (1000 * (point.y + 1)) + 4 * (point.x + 1) + directionScore()

    private fun directionScore() =
        when (facingDirection) {
            UP -> 3
            RIGHT -> 0
            DOWN -> 1
            LEFT -> 2
        }
}

private fun parseMonkeyInstructions(input: String) =
    input.regexParts("""\d+|L|R""").map {
        when (it) {
            "L" -> MonkeyInstruction.Turn.LEFT
            "R" -> MonkeyInstruction.Turn.RIGHT
            else -> MonkeyInstruction.Move(it.toInt())
        }
    }

private typealias WrappingFn = (PositionInGrove, Grid<GroveTile>, Point2D) -> WrapResult

private data class WrapResult(val newPosition: Point2D, val newDirection: FacingDirection)

private fun move(
    grid: Grid<GroveTile>,
    atStartOfMove: PositionInGrove,
    steps: Int,
    wrap: WrappingFn,
): PositionInGrove {
    return (0 until steps).fold(atStartOfMove) { currentPosition, _ ->
        val forward = currentPosition.forwardPosition()

        val needsWrap =
            when (currentPosition.facingDirection) {
                LEFT,
                RIGHT -> forward.x !in grid.rowBounds(forward.y)
                UP,
                DOWN -> forward.y !in grid.columnBounds(forward.x)
            }

        val wrapped =
            if (needsWrap) {
                val after = wrap(currentPosition, grid, forward)
                println("Wrapped $after")
                after
            } else {
                WrapResult(forward, currentPosition.facingDirection)
            }

        val tile =
            grid.tileAt(wrapped.newPosition)
                ?: error("Did not land on valid tile after wrapping ${wrapped.newPosition}")
        if (tile.data == GroveTile.WALL) {
            return currentPosition
        } else {
            atStartOfMove.copy(point = wrapped.newPosition, facingDirection = wrapped.newDirection)
        }
    }
}

private fun executeInstructions(
    start: PositionInGrove,
    grid: Grid<GroveTile>,
    instructions: List<MonkeyInstruction>,
    wrap: WrappingFn,
) =
    instructions.fold(start) { currentPosition, ins ->
        when (ins) {
            is MonkeyInstruction.Move -> move(grid, currentPosition, ins.steps, wrap)
            is MonkeyInstruction.Turn ->
                currentPosition.copy(facingDirection = currentPosition.facingDirection.turn(ins))
        }
    }

private enum class CubeFace(val cubeX: Int, val cubeY: Int) {
    ONE(1, 0),
    TWO(2, 0),
    THREE(1, 1),
    FOUR(0, 2),
    FIVE(1, 2),
    SIX(0, 3);

    fun top(faceSize: Int) = cubeY * faceSize
    fun bottom(faceSize: Int) = top(faceSize) + faceSize - 1

    fun left(faceSize: Int) = cubeX * faceSize
    fun right(faceSize: Int) = left(faceSize) + faceSize - 1
}

fun main() {
    val input = readResourceAsString("/day22.txt")
    val (asciiGrid, rawInstructions) = input.split("\n\n")
    val instructions = parseMonkeyInstructions(rawInstructions)

    val grid =
        parseGridWithEmptyTiles(asciiGrid) {
            when (it) {
                '#' -> GroveTile.WALL
                '.' -> GroveTile.FLOOR
                else -> error("Unknown tile: '$it'")
            }
        }

    val startX = grid.rowBounds(0).start

    part1 {
        fun wrap(
            currentPosition: PositionInGrove,
            grid: Grid<GroveTile>,
            point: Point2D
        ): WrapResult {
            fun wrapX(): Int {
                val xRange = grid.rowBounds(point.y)
                val xOffset = xRange.start
                val xLength = xRange.endInclusive - xRange.start + 1

                return ((point.x - xOffset).mod(xLength)) + xOffset
            }

            fun wrapY(): Int {
                val yRange = grid.columnBounds(point.x)
                val yOffset = yRange.start
                val yLength = yRange.endInclusive - yRange.start + 1

                return ((point.y - yOffset).mod(yLength)) + yOffset
            }

            return when (currentPosition.facingDirection) {
                RIGHT -> Point2D(wrapX(), point.y)
                LEFT -> Point2D(wrapX(), point.y)
                UP -> Point2D(point.x, wrapY())
                DOWN -> Point2D(point.x, wrapY())
            }.let { WrapResult(it, currentPosition.facingDirection) }
        }

        val start = PositionInGrove(Point2D(startX, 0), RIGHT)
        val finalPosition = executeInstructions(start, grid, instructions, ::wrap)

        finalPosition.score()
    }

    part2 {
        val faceSize = 50
        fun wrap(
            currentPosition: PositionInGrove,
            unused1: Grid<GroveTile>,
            unused2: Point2D
        ): WrapResult {
            val currentFace = currentPosition.cubeFace(faceSize)
            val facingDirection = currentPosition.facingDirection
            val point = currentPosition.point

            val xOffsetInFace = point.x - currentFace.left(faceSize)
            val yOffsetInFace = point.y - currentFace.top(faceSize)

            val identity = WrapResult(point, facingDirection)
            return when (currentFace) {
                ONE ->
                    when (facingDirection) {
                        UP ->
                            WrapResult(
                                Point2D(SIX.left(faceSize), SIX.top(faceSize) + xOffsetInFace),
                                RIGHT)
                        RIGHT -> identity
                        DOWN -> identity
                        LEFT ->
                            WrapResult(
                                Point2D(FOUR.left(faceSize), FOUR.bottom(faceSize) - yOffsetInFace),
                                RIGHT)
                    }
                TWO ->
                    when (facingDirection) {
                        UP ->
                            WrapResult(
                                Point2D(SIX.left(faceSize) + xOffsetInFace, SIX.bottom(faceSize)),
                                UP)
                        RIGHT ->
                            WrapResult(
                                Point2D(
                                    FIVE.right(faceSize), FIVE.bottom(faceSize) - yOffsetInFace),
                                LEFT)
                        DOWN ->
                            WrapResult(
                                Point2D(THREE.right(faceSize), THREE.top(faceSize) + xOffsetInFace),
                                LEFT)
                        LEFT -> identity
                    }
                THREE ->
                    when (facingDirection) {
                        UP -> identity
                        RIGHT ->
                            WrapResult(
                                Point2D(TWO.left(faceSize) + yOffsetInFace, TWO.bottom(faceSize)),
                                UP)
                        DOWN -> identity
                        LEFT ->
                            WrapResult(
                                Point2D(FOUR.left(faceSize) + yOffsetInFace, FOUR.top(faceSize)),
                                DOWN)
                    }
                FOUR ->
                    when (facingDirection) {
                        UP ->
                            WrapResult(
                                Point2D(THREE.left(faceSize), THREE.top(faceSize) + xOffsetInFace),
                                RIGHT)
                        RIGHT -> identity
                        DOWN -> identity
                        LEFT ->
                            WrapResult(
                                Point2D(ONE.left(faceSize), ONE.bottom(faceSize) - yOffsetInFace),
                                RIGHT)
                    }
                FIVE ->
                    when (facingDirection) {
                        UP -> identity
                        RIGHT ->
                            WrapResult(
                                Point2D(TWO.right(faceSize), TWO.bottom(faceSize) - yOffsetInFace),
                                LEFT)
                        DOWN ->
                            WrapResult(
                                Point2D(SIX.right(faceSize), SIX.top(faceSize) + xOffsetInFace),
                                LEFT)
                        LEFT -> identity
                    }
                SIX ->
                    when (facingDirection) {
                        UP -> identity
                        RIGHT ->
                            WrapResult(
                                Point2D(FIVE.left(faceSize) + yOffsetInFace, FIVE.bottom(faceSize)),
                                UP)
                        DOWN ->
                            WrapResult(
                                Point2D(TWO.left(faceSize) + xOffsetInFace, TWO.top(faceSize)),
                                DOWN)
                        LEFT ->
                            WrapResult(
                                Point2D(ONE.left(faceSize) + yOffsetInFace, ONE.top(faceSize)),
                                DOWN)
                    }
            }
        }

        val start = PositionInGrove(Point2D(startX, 0), RIGHT)
        val finalPosition = executeInstructions(start, grid, instructions, ::wrap)

        finalPosition.score()
    }
}
