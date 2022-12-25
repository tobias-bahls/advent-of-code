package algorithms

import java.util.PriorityQueue
import utils.Scored

fun <T : Any> dijkstraPath(block: DijkstraBuilder<T>.() -> Unit): List<T>? {
    val builder = DijkstraBuilder<T>().apply(block)

    return when (val result = builder.buildAndRun()) {
        is DijkstraResult.Failure -> null
        is DijkstraResult.Success -> reconstructPath(builder.start, result.found, result.prev)
    }
}

private fun <T> reconstructPath(start: T, end: T, prev: Map<T, T>): List<T> {
    val path = mutableListOf<T>()

    var current: T? = end
    if (prev[current] != null || current == start) {
        while (current != null) {
            path.add(current)
            current = prev[current]
        }
    }

    return path.reversed()
}

fun <T : Any> dijkstra(block: DijkstraBuilder<T>.() -> Unit): T? {
    val builder = DijkstraBuilder<T>().apply(block)

    return when (val result = builder.buildAndRun()) {
        is DijkstraResult.Failure -> null
        is DijkstraResult.Success -> result.found
    }
}

@DslMarker annotation class DijkstraDsl

@DijkstraDsl
class DijkstraBuilder<T : Any> {
    lateinit var start: T

    var end: T? = null
    private var endCondition: ((T) -> Boolean)? = null

    private lateinit var neighbours: (T) -> List<Scored<T>>

    private var inspection: ((T) -> Unit)? = null

    val effectiveEndCondition: (T) -> Boolean
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

    fun inspect(block: (T) -> Unit) {
        this.inspection = block
    }

    fun buildAndRun(): DijkstraResult<T> {
        val dijkstra = Dijkstra(start, effectiveEndCondition, neighbours, inspection)

        return dijkstra.run()
    }
}

sealed interface DijkstraResult<T> {
    val prev: Map<T, T>

    data class Success<T>(val found: T, override val prev: Map<T, T>) : DijkstraResult<T>
    data class Failure<T>(override val prev: Map<T, T>) : DijkstraResult<T>
}

class Dijkstra<T : Any>(
    private val start: T,
    private val endCondition: (T) -> Boolean,
    private val neighbours: (T) -> List<Scored<T>>,
    private val inspection: ((T) -> Unit)?
) {
    fun run(): DijkstraResult<T> {
        val dist = mutableMapOf<T, Int>()
        val prev = mutableMapOf<T, T>()
        dist[start] = 0
        val queue = PriorityQueue<T>(compareBy { dist[it] })
        queue.add(start)
        while (queue.isNotEmpty()) {
            val u = queue.remove()

            inspection?.let { it(u) }

            if (endCondition(u)) {
                return DijkstraResult.Success(u, prev)
            }

            neighbours(u).forEach {
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
        return DijkstraResult.Failure(prev)
    }
}
