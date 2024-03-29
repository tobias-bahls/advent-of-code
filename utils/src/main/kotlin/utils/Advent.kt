package utils

import kotlin.time.measureTime

enum class Part {
    PART1,
    PART2
}

fun unreachable(text: String? = null): Nothing =
    if (text != null) error("unreachable: $text") else error("unreachable")

fun <T> T.dump(): T = this.also { println(this) }

fun <T> T.dump(prefix: String): T = this.also { println("$prefix: $this") }

fun <T> List<T>.sample(): List<T> = this.subList(0, 3).dump()

fun <K, V> Map<K, V>.dumpMap(prefix: String = ""): Map<K, V> =
    this.also { it.entries.dumpList(prefix) }

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

fun <T> solve(msg: String, block: () -> T): T {
    var result: T
    val duration = measureTime { result = block() }

    if (result != Unit) println("$msg: $result [⏰ $duration]")

    return result
}

@Suppress("UNUSED_PARAMETER")
fun <T> sample(name: String = "Sample", block: () -> T) = solve("Sample", block)

fun <T> part1(block: () -> T) = solve("Part 1", block)

@Suppress("UNUSED_PARAMETER") fun <T> xpart1(block: () -> T) = Unit

fun <T> part2(block: () -> T) = solve("Part 2", block)

@Suppress("UNUSED_PARAMETER") fun <T> xpart2(block: () -> T) = Unit

const val BLOCK = '█'
