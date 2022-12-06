fun List<String>.filterNotBlank() = this.map { it.trim() }.filter { it.isNotBlank() }

fun List<Char>.filterWhitespace() = this.filter { !it.isWhitespace() }

fun List<String>.mapInts() = this.map { it.toInt() }

fun <T> List<T>.toPair(): Pair<T, T> = this.expectSize(2).let { (a, b) -> Pair(a, b) }

fun String.twoParts(char: String): List<String> = this.split(char).expectSize(2)

fun String.twoParts(char: Char): List<String> = this.split(char).expectSize(2)

fun <T> String.twoParts(char: Char, block: (String) -> T): Pair<T, T> =
    this.twoParts(char).map(block).let { (a, b) -> Pair(a, b) }

fun String.match(regex: String): MatchResult.Destructured =
    regex.toRegex().find(this)?.destructured ?: error("$regex did not match $this")

fun <T> List<T>.expectSize(expectedSize: Int): List<T> {
    check(this.size == expectedSize) { "Size of list was ${this.size}, expected $expectedSize " }
    return this
}

fun <T> T.dump(): T = this.also { println(this) }

fun <T> T.dump(prefix: String): T = this.also { println("$prefix: $this") }

fun <T> List<T>.sample(): List<T> = this.subList(0, 3).dump()

fun readResourceAsString(name: String): String =
    object {}::class.java.getResource(name)?.readText() ?: error("Did not find file $name")

fun <T> String.parseLines(parser: (String) -> T): List<T> =
    this.lines().filterNotBlank().map(parser)

fun <T> solve(msg: String, block: () -> T) {
    val result = block()
    if (result != Unit) println("$msg: $result")
}

fun <T> part1(block: () -> T) = solve("Part 1", block)

fun <T> part2(block: () -> T) = solve("Part 2", block)

enum class Part {
    PART1,
    PART2
}
