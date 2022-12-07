package utils

import java.util.Stack
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

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

fun String.twoParts(char: String): List<String> = this.split(char).expectSize(2)

fun String.twoParts(char: Char): List<String> = this.split(char).expectSize(2)

fun String.isLowercase(): Boolean = this.lowercase() == this

fun String.isUppercase(): Boolean = this.uppercase() == this

fun <T> String.twoParts(char: Char, block: (String) -> T): Pair<T, T> =
    this.twoParts(char).map(block).let { (a, b) -> Pair(a, b) }

fun String.match(regex: String): MatchResult.Destructured =
    regex.toRegex().find(this)?.destructured ?: error("$regex did not match $this")

fun MatchResult.Destructured.toPair(): Pair<String, String> = this.toList().toPair()

fun <T> List<T>.expectSize(expectedSize: Int): List<T> {
    check(this.size == expectedSize) { "Size of list was ${this.size}, expected $expectedSize " }
    return this
}

fun <T> List<T>.expectOddSize(): List<T> {
    check(this.size % 2 == 1) { "Size of list was expected to be odd, was ${this.size}." }
    return this
}

fun <T> List<T>.middleElement(): T = this.expectOddSize()[this.size / 2]

fun <T> T.dump(): T = this.also { println(this) }

fun <T> T.dump(prefix: String): T = this.also { println("$prefix: $this") }

fun <T> List<T>.sample(): List<T> = this.subList(0, 3).dump()

fun readResourceAsString(name: String): String =
    object {}::class.java.getResource(name)?.readText() ?: error("Did not find file $name")

fun <T> String.parseLines(parser: (String) -> T): List<T> =
    this.lines().filterNotBlank().map(parser)

@OptIn(ExperimentalTime::class)
fun <T> solve(msg: String, block: () -> T) {
    var result: T
    val duration = measureTime { result = block() }

    if (result != Unit) println("$msg: $result [⏰ $duration]")
}

fun <T> sample(name: String = "Sample", block: () -> T) = solve("Sample", block)

fun <T> part1(block: () -> T) = solve("Part 1", block)

fun <T> part2(block: () -> T) = solve("Part 2", block)

enum class Part {
    PART1,
    PART2
}

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

fun Int.times() = (1..this)

fun untilTrue(condition: () -> Boolean): Int {
    var iterations = 0

    while (!condition()) iterations++

    return iterations
}

fun <A, B, RA, RB> Pair<A, B>.transform(block: (Pair<A, B>) -> Pair<RA, RB>): Pair<RA, RB> =
    block(this)

fun <A, R> Pair<A, A>.map(block: (A) -> R): Pair<R, R> = Pair(block(this.first), block(this.second))

fun <A, B, R> Pair<A, B>.mapFirst(block: (A) -> R): Pair<R, B> =
    Pair(block(this.first), this.second)

fun <A, B, R> Pair<A, B>.mapSecond(block: (B) -> R): Pair<A, R> =
    Pair(this.first, block(this.second))
