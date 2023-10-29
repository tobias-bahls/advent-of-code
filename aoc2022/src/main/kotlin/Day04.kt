import utils.fullyContains
import utils.map
import utils.overlaps
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.twoParts

data class AssignmentPair(val first: IntRange, val second: IntRange) {
    companion object {
        fun parse(input: String): AssignmentPair {
            val (left, right) =
                input.twoParts(',') {
                    val (start, end) = it.twoParts('-').map { p -> p.toInt() }
                    IntRange(start, end)
                }

            return AssignmentPair(left, right)
        }
    }

    fun fullyContained(): Boolean = first.fullyContains(second) || second.fullyContains(first)

    fun overlaps(): Boolean = first.overlaps(second)
}

fun main() {
    val input = readResourceAsString("/day04.txt")
    val parsed = input.parseLines { AssignmentPair.parse(it) }

    part1 { parsed.count { it.fullyContained() } }

    part2 { parsed.count { it.overlaps() } }
}
