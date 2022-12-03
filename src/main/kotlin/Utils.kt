fun List<String>.filterNotBlank() = this.filter { it.isNotBlank() }

fun List<String>.mapInts() = this.map { it.toInt() }

fun <T> List<T>.expectSize(expectedSize: Int): List<T> {
    check(this.size == expectedSize) { "Size of list was ${this.size}, expected $expectedSize " }
    return this
}

fun <T> T.dump(): T = this.also { println(this) }

object Utils

fun readResourceAsString(name: String): String = Utils::class.java.getResource(name)!!.readText()

fun <T> solve(msg: String, block: () -> T) {
    val result = block()
    println("$msg: $result")
}

fun <T> part1(block: () -> T)  = solve("Part 1", block)
fun <T> part2(block: () -> T)  = solve("Part 2", block)
