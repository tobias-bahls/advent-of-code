package algorithms

import utils.Scored
import utils.addWithPriority
import utils.priorityQueue

@DslMarker annotation class AstarDsl

@AstarDsl
class AstarBuilder<T : Any> {
    lateinit var start: T

    var end: T? = null
    private var endCondition: ((T) -> Boolean)? = null

    private lateinit var neighbours: (T) -> List<Scored<T>>
    private lateinit var heuristic: (T) -> Int

    private var inspection: ((T) -> Unit)? = null

    private val effectiveEndCondition: (T) -> Boolean
        get() = run {
            if (endCondition == null && end == null) {
                error("Neither endCondition nor end was set for Dijkstra")
            }

            if (endCondition != null && end != null) {
                error("endCondition AND end were set for Dijkstra")
            }

            endCondition ?: { it == end }
        }

    fun endCondition(block: (T) -> Boolean) {
        this.endCondition = block
    }
    fun neighbours(block: (T) -> List<Scored<T>>) {
        this.neighbours = block
    }

    fun heuristic(block: (T) -> Int) {
        this.heuristic = block
    }

    fun inspect(block: (T) -> Unit) {
        this.inspection = block
    }

    fun buildAndRun(): AstarResult<T> {
        val astar = Astar(start, effectiveEndCondition, heuristic, neighbours, inspection)

        return astar.run()
    }
}

fun <T : Any> astar(block: AstarBuilder<T>.() -> Unit): T? {
    val builder = AstarBuilder<T>().apply(block)

    return when (val result = builder.buildAndRun()) {
        is AstarResult.Failure -> null
        is AstarResult.Success -> result.found
    }
}

sealed interface AstarResult<T> {
    val cameFrom: Map<T, T>

    data class Success<T>(val found: T, override val cameFrom: Map<T, T>) : AstarResult<T>
    data class Failure<T>(override val cameFrom: Map<T, T>) : AstarResult<T>
}

class Astar<T>(
    private val start: T,
    private val endCondition: (T) -> Boolean,
    private val heuristic: (T) -> Int,
    private val neighbours: (T) -> List<Scored<T>>,
    private val inspection: ((T) -> Unit)?
) {

    fun run(): AstarResult<T> {
        val openSet = priorityQueue<T>()

        val cameFrom = mutableMapOf<T, T>()
        val gScore = mutableMapOf<T, Int>().withDefault { Integer.MAX_VALUE }
        val fScore = mutableMapOf<T, Int>().withDefault { Integer.MAX_VALUE }

        gScore[start] = 0
        fScore[start] = heuristic(start)

        openSet.addWithPriority(heuristic(start), start)
        while (openSet.isNotEmpty()) {
            val (priority, current) = openSet.remove()

            if (priority != fScore[current]) {
                continue
            }

            inspection?.let { it(current) }

            if (endCondition(current)) {
                return AstarResult.Success(current, cameFrom)
            }

            neighbours(current).forEach {
                val tentativeGScore = gScore.getValue(current) + it.score
                if (tentativeGScore < gScore.getValue(it.neighbour)) {
                    cameFrom[it.neighbour] = current
                    gScore[it.neighbour] = tentativeGScore

                    val neighbourfScore = tentativeGScore + heuristic(it.neighbour)
                    fScore[it.neighbour] = neighbourfScore

                    openSet.addWithPriority(neighbourfScore, it.neighbour)
                }
            }
        }

        return AstarResult.Failure(cameFrom)
    }
}
