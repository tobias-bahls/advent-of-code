package utils

import java.util.PriorityQueue
import java.util.Stack

fun <T> visitAllNodes(initial: List<T>, produceNodes: (T) -> Collection<T>): Collection<T> {
    val visited = mutableSetOf<T>()
    val queue = Stack<T>()

    queue += initial

    while (queue.isNotEmpty()) {
        val elem = queue.pop()
        if (elem !in visited) {
            visited += elem
            queue += produceNodes(elem)
        }
    }

    return visited
}

fun untilTrue(condition: () -> Boolean): Int {
    var iterations = 0

    while (!condition()) iterations++

    return iterations
}

fun <T> reduceTimes(n: Int, initial: T, operation: (T) -> T) =
    1.rangeTo(n).fold(initial) { it, _ -> operation(it) }

fun <T> repeatedList(n: Int, block: () -> T) = 1.rangeTo(n).map { block() }

data class Scored<T>(val score: Int, val neighbour: T)

fun <T> genericDijkstra(start: T, end: T, neighbours: (T) -> List<Scored<T>>): List<T>? {
    val dist = mutableMapOf<T, Int>()
    val prev = mutableMapOf<T, T>()

    dist[start] = 0
    val queue = PriorityQueue<T>(compareBy { dist[it] })
    queue.add(start)

    while (queue.isNotEmpty()) {
        val u = queue.remove()
        if (u == end) {
            return reconstructPath(start, end, prev)
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

    return null
}

private fun <T> reconstructPath(start: T, end: T, prev: MutableMap<T, T>): List<T> {
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
