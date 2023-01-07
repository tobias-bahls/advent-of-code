package utils

import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

enum class Part {
    PART1,
    PART2
}

fun <T> T.dump(): T = this.also { println(this) }

fun <T> T.dump(prefix: String): T = this.also { println("$prefix: $this") }

fun <T> List<T>.sample(): List<T> = this.subList(0, 3).dump()

fun <T> Iterable<T>.dumpList(prefix: String = ""): Iterable<T> {
    if (prefix != "") {
        println("$prefix:")
    } else {
        println("Iterable:")
    }

    return this.onEach { println("  - $it") }
}

fun readResourceAsString(name: String): String =
    object {}::class.java.getResource(name)?.readText() ?: error("Did not find file $name")

@OptIn(ExperimentalTime::class)
fun <T> solve(msg: String, block: () -> T): T {
    var result: T
    val duration = measureTime { result = block() }

    if (result != Unit) println("$msg: $result [⏰ $duration]")

    return result
}

fun <T> sample(name: String = "Sample", block: () -> T) = solve("Sample", block)

fun <T> part1(block: () -> T) = solve("Part 1", block)

fun <T> part2(block: () -> T) = solve("Part 2", block)

const val BLOCK = '█'
