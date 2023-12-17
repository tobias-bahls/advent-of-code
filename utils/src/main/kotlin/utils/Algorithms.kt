package utils

import algorithms.repeat
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

fun repeatUntilTrue(condition: (currentIteration: Int) -> Boolean) =
    repeat(Unit) {
            if (condition(currentIteration)) {
                stop()
            } else {
                next(Unit)
            }
        }
        .iterations

fun <T> reduceTimes(n: Int, initial: T, operation: (T) -> T) =
    1.rangeTo(n).fold(initial) { it, _ -> operation(it) }

fun <T> reduceTimes(n: Long, initial: T, operation: (T) -> T) =
    1.rangeTo(n).fold(initial) { it, _ -> operation(it) }

fun <T> repeatedList(n: Int, block: () -> T) = 1.rangeTo(n).map { block() }

data class Scored<T>(val score: Int, val neighbour: T)

fun <T> T.scored(score: Int) = Scored(score, this)

fun <T> floodFill(initial: T, determineNeighbours: (T) -> List<T>): Set<T> =
    floodFill(initial, determineNeighbours, listOf { _ -> false })

fun <T> floodFill(
    initial: T,
    determineNeighbours: (T) -> List<T>,
    extraStopConditions: List<(T) -> Boolean>
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
