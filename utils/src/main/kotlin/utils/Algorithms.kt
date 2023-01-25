package utils

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

fun untilTrue(condition: (currentIteration: Int) -> Boolean): Int {
    var iterations = 0

    while (!condition(iterations)) iterations++

    return iterations
}

fun <T> reduceTimes(n: Int, initial: T, operation: (T) -> T) =
    1.rangeTo(n).fold(initial) { it, _ -> operation(it) }

fun <T> reduceTimes(n: Long, initial: T, operation: (T) -> T) =
    1.rangeTo(n).fold(initial) { it, _ -> operation(it) }

fun <T> repeatedList(n: Int, block: () -> T) = 1.rangeTo(n).map { block() }

data class Scored<T>(val score: Int, val neighbour: T)

fun <T> floodFill(
    initial: T,
    determineNeighbours: (T) -> List<T>,
    extraStopConditions: List<(T) -> Boolean> = listOf { _ -> false }
): Set<T> {
    val visited = mutableSetOf<T>()

    queue(initial) { elem ->
        if (elem in visited) {
            return@queue skip()
        }
        if (extraStopConditions.any { it(elem) }) {
            return@queue skip()
        }

        visited += elem

        enqueue(determineNeighbours(elem))
    }

    return visited.toSet()
}
