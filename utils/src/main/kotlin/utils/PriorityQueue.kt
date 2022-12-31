package utils

import java.util.PriorityQueue

data class Prioritized<T>(val priority: Int, val value: T)

fun <T> priorityQueue() = PriorityQueue<Prioritized<T>>(compareBy { it.priority })

fun <T> PriorityQueue<Prioritized<T>>.addWithPriority(priority: Int, value: T) =
    add(Prioritized(priority, value))
