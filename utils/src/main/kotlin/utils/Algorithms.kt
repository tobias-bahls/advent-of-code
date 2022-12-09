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

fun untilTrue(condition: () -> Boolean): Int {
    var iterations = 0

    while (!condition()) iterations++

    return iterations
}

fun <T> reduceTimes(n: Int, initial: T, operation: (T) -> T) =
    1.rangeTo(n).fold(initial) { it, _ -> operation(it) }

fun <T> repeatedList(n: Int, block: () -> T) = 1.rangeTo(n).map { block() }
