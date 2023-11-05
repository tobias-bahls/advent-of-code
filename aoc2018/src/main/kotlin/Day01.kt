import utils.*

fun main() {
    val frequencies = readResourceAsString("/day01.txt").parseLines { it.toInt() }
    part1 { frequencies.sum() }

    part2 {
        data class Accumulator(val current: Int = 0, val seen: List<Int> = listOf(0)) {
            operator fun plus(n: Int) = Accumulator(current + n, seen + (current + n))
        }
        frequencies
            .cycle()
            .runningFold(Accumulator()) { acc, i -> acc + i }
            .first { acc -> acc.seen.count { it == acc.current } == 2 }
    }
}
