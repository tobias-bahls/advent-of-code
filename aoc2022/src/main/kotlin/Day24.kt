import datastructures.CardinalDirection.*
import datastructures.Point2D
import datastructures.movementVector
import datastructures.parseAsciiGrid
import java.util.PriorityQueue
import utils.Scored
import utils.lcm
import utils.part1
import utils.part2
import utils.readResourceAsString

private data class Blizzard(val position: Point2D, val direction: CardinalDirectionOrthogonal) {
    fun move(newPosition: Point2D) = copy(position = newPosition)
}

private fun parseBlizzards(input: String): List<Blizzard> =
    parseAsciiGrid(input) { x, y, char ->
        val position = Point2D(x, y)
        when (char) {
            '^' -> Blizzard(position, North)
            '>' -> Blizzard(position, East)
            'v' -> Blizzard(position, South)
            '<' -> Blizzard(position, West)
            else -> null
        }
    }

private data class MapBounds(val width: Int, val height: Int) {
    val targetPoint
        get() = Point2D(width - 2, height - 1)

    val startPoint
        get() = Point2D(1, 0)

    operator fun contains(point: Point2D): Boolean {
        if (point == targetPoint || point == startPoint) {
            return true
        }

        return point.x > 0 && point.x < width - 1 && point.y > 0 && point.y < height - 1
    }
}

private fun parseMapBounds(input: String) =
    MapBounds(input.trim().lines().first().trim().length, input.trim().lines().size)

private data class MapState(val blizzards: List<Blizzard>) {
    val blizzardPoints = blizzards.map { it.position }.toSet()
}

private data class Day24State(val time: Int, val elfPosition: Point2D, val mapBounds: MapBounds) {
    fun tick(newElfPosition: Point2D) = copy(time = time + 1, elfPosition = newElfPosition)

    fun possibleElfMoves(mapState: MapState): List<Point2D> {
        val possiblePositions =
            CardinalDirectionOrthogonal.ALL.map { elfPosition + it.movementVector } + elfPosition

        return possiblePositions
            .filter { it !in mapState.blizzardPoints }
            .filter { it in mapBounds }
    }

    fun score() = elfPosition.manhattanDistanceTo(mapBounds.targetPoint) + time
}

private fun moveBlizzards(blizzards: MapState, mapBounds: MapBounds): MapState {
    return blizzards.blizzards
        .map { blizzard ->
            val targetPosition = blizzard.position + blizzard.direction.movementVector

            val newPosition =
                when {
                    targetPosition.x == mapBounds.width - 1 -> Point2D(1, targetPosition.y)
                    targetPosition.x == 0 -> Point2D(mapBounds.width - 2, targetPosition.y)
                    targetPosition.y == 0 -> Point2D(targetPosition.x, mapBounds.height - 2)
                    targetPosition.y == mapBounds.height - 1 -> Point2D(targetPosition.x, 1)
                    else -> targetPosition
                }

            blizzard.move(newPosition)
        }
        .let { MapState(it) }
}

fun main() {
    val input = readResourceAsString("/day24.txt")

    val mapBounds = parseMapBounds(input)

    val blizzardCycle = (mapBounds.width - 2).lcm(mapBounds.height - 2)
    val blizzardCache = mutableMapOf<Int, MapState>()

    blizzardCache[0] = MapState(parseBlizzards(input))
    (1..blizzardCycle).map { n ->
        blizzardCache[n] = moveBlizzards(blizzardCache[n - 1]!!, mapBounds)
    }

    fun solve(
        startTime: Int,
        startPosition: Point2D,
        targetPosition: Point2D,
        mapBounds: MapBounds
    ): Day24State {
        val startState = Day24State(startTime, startPosition, mapBounds)
        return dijkstra {
            start(startState)
            endCondition { it.elfPosition == targetPosition }

            neighbours { state ->
                val blizzards = blizzardCache[state.time % blizzardCycle]!!
                val possibleElfMoves = state.possibleElfMoves(blizzards)
                possibleElfMoves.map { state.tick(it) }.map { Scored(it.score(), it) }
            }
        }!!
    }
    part1 {
        val elfPosition = mapBounds.startPoint
        solve(0, elfPosition, mapBounds.targetPoint, mapBounds).time - 1
    }

    part2 {
        val startPoint = mapBounds.startPoint
        val firstWay = solve(0, startPoint, mapBounds.targetPoint, mapBounds).time
        val secondWay = solve(firstWay, mapBounds.targetPoint, startPoint, mapBounds).time
        val thirdWay = solve(secondWay, startPoint, mapBounds.targetPoint, mapBounds).time

        thirdWay - 1
    }
}

class Dijkstra<T : Any> {
    lateinit var start: T
    lateinit var endCondition: (T) -> Boolean
    lateinit var neighbours: (T) -> List<Scored<T>>
    var inspection: ((T) -> Unit)? = null

    fun start(start: T) {
        this.start = start
    }
    fun endCondition(block: (T) -> Boolean) {
        this.endCondition = block
    }
    fun neighbours(block: (T) -> List<Scored<T>>) {
        this.neighbours = block
    }

    fun inspect(block: (T) -> Unit) {
        this.inspection = block
    }
}

fun <T : Any> dijkstra(config: Dijkstra<T>.() -> Unit): T? {
    val dijkstra = Dijkstra<T>()
    dijkstra.config()

    val dist = mutableMapOf<T, Int>()
    val prev = mutableMapOf<T, T>()
    dist[dijkstra.start] = 0
    val queue = PriorityQueue<T>(compareBy { dist[it] })
    queue.add(dijkstra.start)
    while (queue.isNotEmpty()) {
        val u = queue.remove()
        if (dijkstra.inspection != null) {
            dijkstra.inspection?.let { it(u) }
        }
        if (dijkstra.endCondition(u)) {
            return u
        }

        dijkstra.neighbours(u).forEach {
            val alt = dist.getOrDefault(u, Integer.MAX_VALUE) + it.score
            if (alt < dist.getOrDefault(it.neighbour, Integer.MAX_VALUE)) {
                dist[it.neighbour] = alt
                prev[it.neighbour] = u
                if (it.neighbour !in queue) {
                    queue.add(it.neighbour)
                }
            }
        }
    }
    return null
}
