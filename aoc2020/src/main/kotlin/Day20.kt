import datastructures.CardinalDirection
import datastructures.Point2D
import datastructures.movementVector
import datastructures.parseAsciiGrid
import utils.QueueMode
import utils.match
import utils.part1
import utils.part2
import utils.queue
import utils.readResourceAsString
import utils.toBitSet
import utils.toRangeBy

private data class CameraTile(val id: Long, val points: Set<Point2D>) {
    private val xRange = points.toRangeBy { it.x }
    private val yRange = points.toRangeBy { it.y }

    val topEdge by lazy { points.filter { it.y == 0 }.map { it.x }.toBitSet() }
    val rightEdge by lazy { points.filter { it.x == xRange.endInclusive }.map { it.y }.toBitSet() }
    val bottomEdge by lazy { points.filter { it.y == yRange.endInclusive }.map { it.x }.toBitSet() }
    val leftEdge by lazy { points.filter { it.x == 0 }.map { it.y }.toBitSet() }

    fun rotate(degrees: Int): CameraTile {
        val rotatedPoints = points.map { it.rotate(Point2D(0, 0), degrees) }.toSet()

        val minY = rotatedPoints.minBy { it.y }.y
        val minX = rotatedPoints.minBy { it.x }.x
        val topLeft = Point2D(minX, minY)

        val normalized =
            if (minY != 0 || minX != 0) {
                rotatedPoints.map { it - topLeft }.toSet()
            } else {
                rotatedPoints
            }

        return copy(points = normalized)
    }

    fun flipHorizontal(): CameraTile {
        val max = points.maxOf { it.y }

        val newPoints = points.map { Point2D(it.x, max - it.y) }.toSet()

        return copy(points = newPoints)
    }

    fun flipVertical(): CameraTile {
        val max = points.maxOf { it.x }

        val newPoints = points.map { Point2D(max - it.x, it.y) }.toSet()

        return copy(points = newPoints)
    }

    fun neighbourDirection(other: CameraTile): CardinalDirection? {
        return when {
            topEdge == other.bottomEdge -> CardinalDirection.North
            rightEdge == other.leftEdge -> CardinalDirection.East
            bottomEdge == other.topEdge -> CardinalDirection.South
            leftEdge == other.rightEdge -> CardinalDirection.West
            else -> null
        }
    }

    fun removeBorder(): CameraTile {
        val points =
            points.filterNot {
                it.y == 0 || it.x == 0 || it.x == xRange.endInclusive || it.y == yRange.endInclusive
            }

        return CameraTile(id, points.toSet())
    }

    fun applyOffset(offset: Point2D): CameraTile {
        val points = points.map { it + offset }

        return CameraTile(id, points.toSet())
    }
}

private fun parseCameraTile(raw: String): CameraTile {
    val (id) = raw.lines().first().match("""Tile (\d+):""")

    val points =
        parseAsciiGrid(raw.lines().drop(1)) { x, y, char ->
            if (char == '.') null else Point2D(x, y)
        }

    return CameraTile(id.toLong(), points.toSet())
}

private data class PlacedTile(val tile: CameraTile, val point: Point2D)

private data class Day20State(val placedTiles: List<PlacedTile>, val cameraTiles: Set<CameraTile>) {
    val tilesByPoint = placedTiles.associateBy { it.point }
    val occupiedPoints = placedTiles.map { it.point }.toSet()
    val tiles = placedTiles.map { it.tile }.toSet()
    val complete = placedTiles.size == cameraTiles.size
    val openTiles = cameraTiles - tiles

    fun isEmpty(point: Point2D) = point !in occupiedPoints

    fun canPlace(tile: CameraTile, point: Point2D): Boolean {
        if (!isEmpty(point)) {
            return false
        }

        return CardinalDirection.CardinalDirectionOrthogonal.ALL.all { dir ->
            val tileAt = tileAt(point + dir.movementVector) ?: return@all true

            tile.neighbourDirection(tileAt) == dir
        }
    }
    fun placeTile(tile: CameraTile, point: Point2D) =
        copy(placedTiles = placedTiles + PlacedTile(tile, point))

    fun tileAt(point: Point2D): CameraTile? = tilesByPoint[point]?.tile

    fun solution(): Long {
        val xRange = placedTiles.map { it.point }.toRangeBy { it.x }
        val yRange = placedTiles.map { it.point }.toRangeBy { it.y }

        return listOf(
                tileAt(Point2D(xRange.start, yRange.start)),
                tileAt(Point2D(xRange.start, yRange.endInclusive)),
                tileAt(Point2D(xRange.endInclusive, yRange.start)),
                tileAt(Point2D(xRange.endInclusive, yRange.endInclusive)),
            )
            .map { it!!.id }
            .reduce(Long::times)
    }

    fun mergeImage(): CameraTile {
        val adjusted =
            placedTiles.flatMap { it.tile.removeBorder().applyOffset(it.point * 8).points }.toSet()

        return CameraTile(0, adjusted)
    }
}

private fun calculatePermutations(cameraTile: CameraTile): List<CameraTile> =
    listOf(0, 90, 180, 270)
        .map { cameraTile.rotate(it) }
        .flatMap { listOf(it, it.flipVertical(), it.flipHorizontal()) }

private fun reconstructImage(cameraTiles: Set<CameraTile>): Day20State {
    val permutations =
        cameraTiles
            .flatMap { calculatePermutations(it) }
            .groupingBy { it.id }
            .fold(emptySet<CameraTile>()) { acc, it -> acc + it }

    val initial = Day20State(listOf(PlacedTile(cameraTiles.first(), Point2D.ZERO)), cameraTiles)
    var result: Day20State? = null

    queue(initial, QueueMode.LIFO) { state ->
        if (state.complete) {
            result = state
            return@queue exit()
        }

        val placedAndOpen =
            state.placedTiles.flatMap { placed -> state.openTiles.map { Pair(placed, it) } }

        placedAndOpen
            .flatMap { (placed, open) ->
                permutations[open.id]!!.mapNotNull { openPermutation ->
                    val dir =
                        placed.tile.neighbourDirection(openPermutation) ?: return@mapNotNull null
                    val point = placed.point + dir.movementVector

                    if (state.canPlace(openPermutation, point)) {
                        state.placeTile(openPermutation, point)
                    } else {
                        null
                    }
                }
            }
            .let { enqueue(it) }
    }
    return result!!
}

fun main() {
    val input = readResourceAsString("/day20.txt")
    val cameraTiles = input.split("\n\n").map { parseCameraTile(it) }.toSet()

    part1 { reconstructImage(cameraTiles).solution() }
    part2 {
        val seaMonster =
            """
                              # 
            #    ##    ##    ###
             #  #  #  #  #  #   
            """
                .trimIndent()
                .replace(" ", ".")

        val seaMonsterPattern =
            parseAsciiGrid(seaMonster) { x, y, char -> if (char == '#') Point2D(x, y) else null }

        val seaMonsterWidth = seaMonsterPattern.maxOf { it.x }
        val seaMonsterHeight = seaMonsterPattern.maxOf { it.y }

        val merged = reconstructImage(cameraTiles).mergeImage()

        calculatePermutations(merged)
            .firstNotNullOf { permuted ->
                val yRange = permuted.points.toRangeBy { it.y }
                val xRange = permuted.points.toRangeBy { it.x }

                val effectiveYRange = (yRange.start..yRange.endInclusive - seaMonsterHeight)
                val effectiveXRange = (xRange.start..xRange.endInclusive - seaMonsterWidth)

                effectiveYRange
                    .flatMap { y -> effectiveXRange.map { x -> Point2D(x, y) } }
                    .flatMap { seaMonsterOffset ->
                        val movedSeaMonster =
                            seaMonsterPattern.map { it + seaMonsterOffset }.toSet()

                        if (permuted.points.containsAll(movedSeaMonster)) {
                            movedSeaMonster
                        } else {
                            emptyList()
                        }
                    }
                    .toSet()
                    .let { if (it.isEmpty()) null else permuted.points - it }
            }
            .size
    }
}
