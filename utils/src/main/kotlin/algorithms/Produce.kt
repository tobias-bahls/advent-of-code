package algorithms

sealed interface ProduceOperation<in T> {
    data object Stop : ProduceOperation<Any?>

    data class Next<T>(val elem: T) : ProduceOperation<T>
}

data class ProduceResult<T>(val elements: List<T>)

class ProduceOperations<T> {
    var currentIteration = 0
        private set

    fun next(elem: T) = ProduceOperation.Next(elem)

    fun stop() = ProduceOperation.Stop
}

fun <T> produce(
    initial: T,
    includeInitial: Boolean = false,
    inspect: Boolean = false,
    process: ProduceOperations<T>.(T) -> ProduceOperation<T>
): ProduceResult<T> {
    val elems = mutableListOf<T>()
    var current = initial
    if (includeInitial) {
        elems.add(initial)
    }
    val ops = ProduceOperations<T>()
    while (true) {
        if (inspect) {
            println("${ops.currentIteration}: $current")
        }
        when (val result = ops.process(current)) {
            is ProduceOperation.Next -> {
                current = result.elem
                elems.add(current)
            }
            is ProduceOperation.Stop -> {
                return ProduceResult(elems)
            }
        }
    }
}
