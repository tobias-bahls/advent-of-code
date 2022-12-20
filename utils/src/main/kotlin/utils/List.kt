package utils

fun List<String>.filterNotBlank() = this.map { it.trim() }.filter { it.isNotBlank() }

fun List<Char>.filterWhitespace() = this.filter { !it.isWhitespace() }

fun List<String>.mapInts() = this.map { it.toInt() }

fun <T> List<T>.toPair(): Pair<T, T> = this.expectSize(2).let { (a, b) -> Pair(a, b) }

fun <T> List<T>.applyEach(block: (T) -> Unit): List<T> =
    this.map {
        block(it)
        it
    }

fun <T> List<T>.firstRest(): Pair<T, List<T>> = Pair(this.first(), this.drop(1))

fun <T> List<T>.expectSize(expectedSize: Int): List<T> {
    check(this.size == expectedSize) { "Size of list was ${this.size}, expected $expectedSize " }
    return this
}

fun <T> List<T>.expectOddSize(): List<T> {
    check(this.size % 2 == 1) { "Size of list was expected to be odd, was ${this.size}." }
    return this
}

fun <T> List<T>.middleElement(): T = this.expectOddSize()[this.size / 2]

fun <E> transpose(xs: List<List<E>>): List<List<E>> {
    fun <E> List<E>.head(): E = this.first()
    fun <E> List<E>.tail(): List<E> = this.takeLast(this.size - 1)
    fun <E> E.append(xs: List<E>): List<E> = listOf(this).plus(xs)

    xs.filter { it.isNotEmpty() }
        .let { ys ->
            return when (ys.isNotEmpty()) {
                true -> ys.map { it.head() }.append(transpose(ys.map { it.tail() }))
                else -> emptyList()
            }
        }
}

fun List<Int>.median() =
    sorted().let {
        if (it.size % 2 == 0) {
            (it[it.size / 2] + it[it.size / 2 - 1]) / 2
        } else {
            it[it.size / 2]
        }
    }

fun List<Int>.mean() = sum() / this.size.toDouble()

fun <T> Sequence<T>.cycle(): Sequence<T> = generateSequence(this) { this }.flatten()

fun <T> List<T>.cycle(): Sequence<T> = this.asSequence().cycle()

data class Indexed<T>(val elem: T, val index: Int)

fun <T> List<T>.zipWithIndex(): List<Indexed<T>> =
    this.zip(indices).map { (elem, index) -> Indexed(elem, index) }

fun <T, R : Comparable<R>> Collection<T>.toRangeBy(comparable: (T) -> R): ClosedRange<R> {
    val min = minOf(comparable)
    val max = maxOf(comparable)

    return (min..max)
}

fun <T, R> Iterable<T>.cartesian(other: Iterable<R>): Sequence<Pair<T, R>> =
    this.asSequence().cartesian(other)

fun <T, R> Sequence<T>.cartesian(other: Iterable<R>): Sequence<Pair<T, R>> =
    this.flatMap { a -> other.asSequence().map { b -> a to b } }

fun List<Boolean>.interpretAsBinary() =
    joinToString("") {
            if (it) {
                "1"
            } else {
                "0"
            }
        }
        .toInt(2)

fun <T> List<T>.indexOfOrNull(predicate: (T) -> Boolean): Int? =
    indexOfFirst(predicate).let { if (it == -1) null else it }
