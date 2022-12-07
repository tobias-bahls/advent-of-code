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
