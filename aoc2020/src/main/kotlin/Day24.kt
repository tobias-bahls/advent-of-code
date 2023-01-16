import TileColor.*
import datastructures.CardinalDirection
import datastructures.CardinalDirection.*
import datastructures.Point2D
import datastructures.parseCardinalDirection
import utils.extend
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.reduceTimes
import utils.regexParts
import utils.toRangeBy

private enum class TileColor {
    BLACK,
    WHITE
}

private fun Point2D.pointInDirectionHexagonal(cardinalDirection: CardinalDirection) =
    when (cardinalDirection) {
        East -> this + Point2D(1, 0)
        SouthEast ->
            if (this.y % 2 == 0) {
                this + Point2D(0, 1)
            } else {
                this + Point2D(1, 1)
            }
        SouthWest -> {
            if (this.y % 2 == 0) {
                this + Point2D(-1, 1)
            } else {
                this + Point2D(0, 1)
            }
        }
        NorthWest -> {
            if (this.y % 2 == 0) {
                this + Point2D(-1, -1)
            } else {
                this + Point2D(0, -1)
            }
        }
        NorthEast -> {
            if (this.y % 2 == 0) {
                this + Point2D(0, -1)
            } else {
                this + Point2D(1, -1)
            }
        }
        West -> this + Point2D(-1, 0)
        else -> error("Invalid cardinal direction $cardinalDirection for hex tile ")
    }

private data class Map(val currentPosition: Point2D, val blackTiles: Set<Point2D>) {

    fun moveToStart() = copy(currentPosition = Point2D.ZERO)

    fun move(steps: List<CardinalDirection>) =
        steps.fold(this) { currentMap, it -> currentMap.move(it) }

    fun move(direction: CardinalDirection) =
        copy(currentPosition = currentPosition.pointInDirectionHexagonal(direction))

    fun flip(): Map {
        val existingTile = tileColorAt(currentPosition)

        return if (existingTile == BLACK) {
            copy(blackTiles = blackTiles - currentPosition)
        } else {
            copy(blackTiles = blackTiles + currentPosition)
        }
    }

    private fun tileColorAt(point: Point2D) = if (point in blackTiles) BLACK else WHITE

    fun step(): Map {
        val xRange = blackTiles.toRangeBy { it.x }.extend(2)
        val yRange = blackTiles.toRangeBy { it.y }.extend(2)

        val points =
            (xRange.first..xRange.last).flatMap { x ->
                (yRange.first..yRange.last).map { y -> Point2D(x, y) }
            }

        return points
            .flatMapTo(mutableSetOf()) { point ->
                val tile = tileColorAt(point)
                val blackNeighbours = getNeighbourPoints(point).count { tileColorAt(it) == BLACK }

                when {
                    tile == WHITE && blackNeighbours == 2 -> listOf(point)
                    tile == BLACK && (blackNeighbours == 0 || blackNeighbours > 2) -> emptyList()
                    tile == WHITE -> emptyList()
                    tile == BLACK -> listOf(point)
                    else -> error("Unreachable")
                }
            }
            .let { copy(blackTiles = it) }
    }

    private fun getNeighbourPoints(position: Point2D) =
        listOf(East, SouthEast, SouthWest, NorthWest, NorthEast, West).map {
            position.pointInDirectionHexagonal(it)
        }
}

private fun buildMap(parsed: List<List<CardinalDirection>>): Map {
    val initialMap = Map(Point2D.ZERO, setOf())

    val result =
        parsed.fold(initialMap) { currentMap, directions ->
            val moved = currentMap.move(directions)

            moved.flip().moveToStart()
        }
    return result
}

fun main() {
    val input = readResourceAsString("/day24.txt")
    val parsed =
        input.parseLines {
            it.regexParts("(e|se|sw|w|nw|ne)").map { dir -> parseCardinalDirection(dir) }
        }

    part1 {
        val result = buildMap(parsed)

        result.blackTiles.size
    }
    part2 {
        val map = buildMap(parsed)

        val result = reduceTimes(100, map) { it.step() }
        result.blackTiles.size
    }
}
