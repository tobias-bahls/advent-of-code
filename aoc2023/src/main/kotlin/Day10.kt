import datastructures.*
import datastructures.CardinalDirection.*
import utils.*

private sealed interface Day10Tile {
    data object Start : Day10Tile

    data object Floor : Day10Tile

    data class Pipe(val start: CardinalDirectionOrthogonal, val end: CardinalDirectionOrthogonal) :
        Day10Tile {
        val ends = setOf(start, end)
    }
}

private fun findPath(
    start: Tile<Day10Tile>,
    startDirection: CardinalDirectionOrthogonal
): List<Tile<Day10Tile>> {
    data class State(val current: Tile<Day10Tile>, val path: List<Tile<Day10Tile>>)

    val current = start.directNeighbourInDirection(startDirection)!!
    val initialState = State(current, listOf(current))

    return algorithms
        .repeat(initialState) { state ->
            val nextTile =
                state.current
                    .directNeighboursInDirections((state.current.data as Day10Tile.Pipe).ends)
                    .filter { it.data !is Day10Tile.Floor }
                    .singleOrNull { it !in state.path && it != start }

            if (nextTile == null) {
                stop()
            } else {
                next(State(nextTile, state.path + nextTile))
            }
        }
        .element
        .path
}

private fun parseGridAndReplaceStart(input: String): Pair<Point2D, Grid<Day10Tile>> {
    val grid =
        parseGrid(input) {
            when (it) {
                '.' -> Day10Tile.Floor
                'S' -> Day10Tile.Start
                '|' -> Day10Tile.Pipe(North, South)
                '-' -> Day10Tile.Pipe(East, West)
                'L' -> Day10Tile.Pipe(North, East)
                'J' -> Day10Tile.Pipe(North, West)
                '7' -> Day10Tile.Pipe(South, West)
                'F' -> Day10Tile.Pipe(South, East)
                else -> unreachable("Tile $it")
            }
        }

    val startTile = grid.tiles.first { it.data is Day10Tile.Start }

    val (startPipeStart, startPipeEnd) =
        CardinalDirectionOrthogonal.ALL.mapNotNull { direction ->
            val neighbour =
                (startTile.directNeighbourInDirection(direction)?.data as? Day10Tile.Pipe)
                    ?: return@mapNotNull null

            if (direction.opposite() in neighbour.ends) {
                direction
            } else {
                null
            }
        }

    val pipeGrid =
        Grid(
            grid.tiles.map {
                when (it.data) {
                    is Day10Tile.Start ->
                        Tile(it.point, Day10Tile.Pipe(startPipeStart, startPipeEnd))
                    is Day10Tile.Pipe -> it
                    is Day10Tile.Floor -> it
                }
            })

    return Pair(startTile.point, pipeGrid)
}

fun main() {
    xpart1 {
        val input = readResourceAsString("/day10.txt")
        val (start, grid) = parseGridAndReplaceStart(input)

        val startTile = grid.tileAt(start)!!
        val (startPipeStart, startPipeEnd) = (startTile.data as Day10Tile.Pipe).ends.toList()

        val pathClockwise = findPath(startTile, startPipeStart)
        val pathCounterclockwise = findPath(startTile, startPipeEnd)

        pathClockwise.zip(pathCounterclockwise).indexOfFirst { (step1, step2) -> step1 == step2 } +
            1
    }

    part2 {
        val input = readResourceAsString("/day10.txt")
        val (start, grid) = parseGridAndReplaceStart(input)

        val startTile = grid.tileAt(start)!!
        val startPipeStart = (startTile.data as Day10Tile.Pipe).ends.first()

        val path = findPath(startTile, startPipeStart) + startTile

        data class State(val inside: Boolean, val counter: Int)
        val tiles =
            grid.yRangeProgression.flatMap { y ->
                grid.xRangeProgression.map { x -> grid.tileAt(Point2D(x, y))!! }
            }

        tiles
            .fold(State(false, 0)) { state, tile ->
                var newInside = state.inside
                if (tile in path && North in (tile.data as Day10Tile.Pipe).ends) {
                    newInside = !newInside
                }

                if (newInside && tile !in path) {
                    State(newInside, state.counter + 1)
                } else {
                    State(newInside, state.counter)
                }
            }
            .counter
    }
}
