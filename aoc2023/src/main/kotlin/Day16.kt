import Day16Tile.*
import datastructures.*
import datastructures.CardinalDirection.*
import utils.*

private enum class Day16Tile {
    FLOOR,
    MIRROR_UP,
    MIRROR_DOWN,
    SPLITTER_VERTICAL,
    SPLITTER_HORIZONTAL,
}

private data class PointAndDirection(
    val point: Point2D,
    val direction: CardinalDirectionOrthogonal
)

private data class BeamHead(val point: Point2D, val direction: CardinalDirectionOrthogonal) {
    val pointAndDirection = PointAndDirection(point, direction)

    fun advance() = copy(point = point + direction.movementVector)

    fun changeDirection(newDirection: CardinalDirectionOrthogonal) = copy(direction = newDirection)

    fun split(vararg dirs: CardinalDirectionOrthogonal) = dirs.map { copy(direction = it) }
}

private fun castLight(
    grid: Grid<Day16Tile>,
    startPoint: Point2D,
    startDirection: CardinalDirectionOrthogonal
): Int {
    data class State(
        val activeBeams: List<BeamHead>,
        val finishedBeams: List<BeamHead>,
        val seen: Set<PointAndDirection>
    )
    val initialBeam = BeamHead(startPoint, startDirection)
    val initialState = State(listOf(initialBeam), emptyList(), setOf(initialBeam.pointAndDirection))

    val result =
        algorithms.repeat(initialState) { state ->
            if (state.activeBeams.isEmpty()) {
                return@repeat stop()
            }

            val result =
                state.activeBeams.flatMap { beam ->
                    val tile = grid.tileAt(beam.point) ?: error("landed out of bounds: $beam")
                    val data = tile.data

                    when (data) {
                        FLOOR -> listOf(beam)
                        MIRROR_UP -> {
                            when (beam.direction) {
                                East -> beam.changeDirection(South)
                                North -> beam.changeDirection(West)
                                South -> beam.changeDirection(East)
                                West -> beam.changeDirection(North)
                            }.let { listOf(it) }
                        }
                        MIRROR_DOWN -> {
                            when (beam.direction) {
                                East -> beam.changeDirection(North)
                                North -> beam.changeDirection(East)
                                South -> beam.changeDirection(West)
                                West -> beam.changeDirection(South)
                            }.let { listOf(it) }
                        }
                        SPLITTER_VERTICAL ->
                            when (beam.direction) {
                                East,
                                West -> beam.split(North, South)
                                North,
                                South -> listOf(beam)
                            }
                        SPLITTER_HORIZONTAL ->
                            when (beam.direction) {
                                North,
                                South -> beam.split(East, West)
                                East,
                                West -> listOf(beam)
                            }
                    }.map { it.advance() }
                }

            val activeBeams =
                result.filter { it.pointAndDirection !in state.seen && grid.inBounds(it.point) }
            val finishedBeams =
                result.filter { it.pointAndDirection in state.seen || !grid.inBounds(it.point) } +
                    state.finishedBeams

            next(
                State(activeBeams, finishedBeams, state.seen + result.map { it.pointAndDirection }))
        }

    return result.element.seen.map { it.point }.filter { grid.inBounds(it) }.toSet().size
}

private fun parseDay16Grid(str: String) =
    parseGrid(str) {
        when (it) {
            '.' -> FLOOR
            '\\' -> MIRROR_UP
            '/' -> MIRROR_DOWN
            '|' -> SPLITTER_VERTICAL
            '-' -> SPLITTER_HORIZONTAL
            else -> unreachable("$it")
        }
    }

fun main() {
    part1 {
        val input = readResourceAsString("/day16.txt")
        val grid = parseDay16Grid(input)

        castLight(grid, Point2D.ZERO, East)
    }

    part2 {
        val input = readResourceAsString("/day16.txt")
        val grid = parseDay16Grid(input)

        val tries =
            grid.edges().flatMap { tile ->
                listOfNotNull(
                        if (tile in grid.northEdge()) South else null,
                        if (tile in grid.eastEdge()) West else null,
                        if (tile in grid.southEdge()) North else null,
                        if (tile in grid.westEdge()) East else null,
                    )
                    .map { PointAndDirection(tile.point, it) }
            }

        tries.maxOf { castLight(grid, it.point, it.direction) }
    }
}
