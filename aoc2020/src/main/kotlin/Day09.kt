import utils.cartesian
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString

fun main() {
    val input = readResourceAsString("/day09.txt")
    val parsed = input.parseLines { it.toLong() }

    val p1Result = part1 {
        val preambleLength = 25
        parsed
            .windowed(preambleLength + 1)
            .find {
                val preamble = it.slice(0 until preambleLength)
                val num = it[preambleLength]
                val preambleSums = preamble.cartesian(preamble).map { (a, b) -> a + b }.toSet()

                num !in preambleSums
            }
            ?.last() ?: error("Could not find result")
    }

    part2 {
        parsed.indices
            .asSequence()
            .map { index ->
                var sumSoFar = 0L
                parsed.drop(index).takeWhile {
                    sumSoFar += it

                    sumSoFar <= p1Result
                }
            }
            .find { it.sum() == p1Result }
            ?.let { it.min() + it.max() }
    }
}
