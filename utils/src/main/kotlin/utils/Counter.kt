package utils

class Counter<T> {
    private var _counts = mutableMapOf<T, Int>()

    val counts
        get(): Map<T, Int> = _counts

    fun increment(elem: T) {
        incrementBy(elem, 1)
    }

    fun increment(elems: Collection<T>) {
        elems.forEach { increment(it) }
    }

    operator fun contains(elem: T) = counts.containsKey(elem)

    fun incrementBy(elem: T, by: Int) {
        if (_counts[elem] == null) {
            _counts[elem] = by
        } else {
            _counts[elem] = _counts.getValue(elem) + by
        }
    }

    override fun toString() = counts.toString()
}
