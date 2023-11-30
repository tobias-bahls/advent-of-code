package algorithms

sealed interface RepeatOperation<in T> {
    data object Stop : RepeatOperation<Any?>

    data class Next<T>(val elem: T) : RepeatOperation<T>
}

data class RepeatResult<T>(val iterations: Int, val element: T)

class RepeatOperations<T> {
    var currentIteration = 0
        private set

    fun next(elem: T) = RepeatOperation.Next(elem)

    fun stop() = RepeatOperation.Stop

    fun incrementIteration() {
        currentIteration++
    }
}

fun <T> repeat(
    initial: T,
    inspect: Boolean = false,
    process: RepeatOperations<T>.(T) -> RepeatOperation<T>
): RepeatResult<T> {

    var current = initial
    val ops = RepeatOperations<T>()
    while (true) {
        if (inspect) {
            println("${ops.currentIteration}: $current")
        }
        when (val result = ops.process(current)) {
            is RepeatOperation.Next -> {
                current = result.elem
            }
            is RepeatOperation.Stop -> {
                return RepeatResult(ops.currentIteration, current)
            }
        }
        ops.incrementIteration()
    }
}
