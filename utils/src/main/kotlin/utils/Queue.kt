package utils

enum class QueueMode {
    LIFO,
    FIFO
}

sealed interface QueueOperation<in T> {
    data class Enqueue<T>(val elems: Collection<T>) : QueueOperation<T>
    object Skip : QueueOperation<Any?>
    object Exit : QueueOperation<Any?>
}

class QueueOperations<T> {
    fun enqueue(elem: T) = QueueOperation.Enqueue(listOf(elem))
    fun enqueue(elems: Collection<T>) = QueueOperation.Enqueue(elems)
    fun skip() = QueueOperation.Skip
    fun exit() = QueueOperation.Exit
}

fun <T> queue(
    initial: T,
    mode: QueueMode = QueueMode.FIFO,
    process: QueueOperations<T>.(T) -> QueueOperation<T>
) {
    val queue = ArrayDeque<T>()
    queue.add(initial)

    val add: (T) -> Unit =
        when (mode) {
            QueueMode.LIFO -> ({ queue.addFirst(it) })
            QueueMode.FIFO -> ({ queue.addLast(it) })
        }

    val ops = QueueOperations<T>()
    while (queue.isNotEmpty()) {
        val elem = queue.removeFirst()

        when (val result = ops.process(elem)) {
            is QueueOperation.Enqueue<T> -> result.elems.forEach { add(it) }
            is QueueOperation.Skip -> Unit
            is QueueOperation.Exit -> return
        }
    }
}
