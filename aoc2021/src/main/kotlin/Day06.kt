import utils.part1
import utils.part2
import utils.readResourceAsString

fun step(counts: List<Long>): List<Long> =
    listOf(
        counts[1],
        counts[2],
        counts[3],
        counts[4],
        counts[5],
        counts[6],
        counts[7] + counts[0],
        counts[8],
        counts[0],
    )

fun main() {
    val input: List<Long> =
        readResourceAsString("/day06.txt")
            .lines()
            .first { it.isNotBlank() }
            .split(",")
            .map { Integer.parseInt(it) }
            .fold(MutableList(9) { 0 }) { acc, elem ->
                acc[elem] = acc[elem] + 1
                acc
            }

    part1 { (0 until 80).fold(input) { acc, i -> step(acc) }.sum() }
    part2 { (0 until 256).fold(input) { acc, i -> step(acc) }.sum() }
}
