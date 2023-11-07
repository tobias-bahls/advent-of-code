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

fun <T> List<T>.head(): T = this.first()

fun <T> List<T>.tail(): List<T> = this.takeLast(this.size - 1)

fun <T> T.append(xs: List<T>): List<T> = listOf(this).plus(xs)

fun <T> List<List<T>>.transpose(): List<List<T>> =
    this.filter { it.isNotEmpty() }
        .let { ys ->
            return when (ys.isNotEmpty()) {
                true -> ys.map { it.head() }.append(ys.map { it.tail() }.transpose())
                else -> emptyList()
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

fun <T> List<T>.cycleGet(index: Int): T = get(index % size)

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

fun <T> Iterable<T>.consecutiveEqualElements(): Sequence<List<T>> {
    var sequence = this.asSequence()

    return generateSequence {
        val current = sequence.firstOrNull() ?: return@generateSequence null

        val consecutive = sequence.takeWhile { it == current }
        sequence = sequence.dropWhile { it == current }

        consecutive.toList()
    }
}

fun <T> Iterable<T>.chunksDelimitedBy(block: (T) -> Boolean): Sequence<List<T>> {
    var sequence = this.asSequence()

    return generateSequence {
        val delimiter = sequence.take(1).firstOrNull()
        if (delimiter == null || !block(delimiter)) {
            return@generateSequence null
        }

        val consecutive = sequence.drop(1).takeWhile { !block(it) }
        sequence = sequence.drop(1).dropWhile { !block(it) }

        (sequenceOf(delimiter) + consecutive).toList()
    }
}

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

fun <T> List<T>.updated(index: Int, element: T) = mapIndexed { idx, elem ->
    if (index == idx) element else elem
}

fun <T> List<T>.removeAt(index: Int) = mapIndexedNotNull { idx, elem ->
    if (index == idx) null else elem
}

fun <T> T?.nullableToList(): List<T> = if (this == null) emptyList() else listOf(this)

fun <T> List<T>.toFrequencyMap() = this.groupingBy { it }.eachCount()

fun <T> Iterable<T>.permutations(): Iterable<List<T>> {

    fun inner(current: List<T>, remaining: List<T>): List<List<T>> {
        if (remaining.isEmpty()) return listOf(current)
        return remaining.flatMapIndexed { idx, it -> inner(current + it, remaining.removeAt(idx)) }
    }

    return inner(emptyList(), this.toList())
}
