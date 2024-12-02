import kotlin.math.absoluteValue
import utils.*

fun isSafe(report: List<Int>): Boolean {
    val allIncreasing = report.zipWithNext().all { (a, b) -> a < b }
    val allDecreasing = report.zipWithNext().all { (a, b) -> a > b }
    val allSafeDifference =
        report.zipWithNext().all { (a, b) ->
            val diff = (a - b).absoluteValue

            diff in 1..3
        }

    return (allIncreasing || allDecreasing) && allSafeDifference
}

fun main() {
    val sample =
        """
        7 6 4 2 1
        1 2 7 8 9
        9 7 6 2 1
        1 3 2 4 5
        8 6 4 4 1
        1 3 6 7 9
    """
            .trimIndent()

    part1 {
        val input = readResourceAsString("/day02.txt")
        val reports = input.parseLines { it.split(" ").mapInts() }

        reports.count { isSafe(it) }
    }

    part2 {
        val input = readResourceAsString("/day02.txt")
        val reports = input.parseLines { it.split(" ").mapInts() }

        reports.count { report ->
            if (isSafe(report)) {
                true
            } else {
                report.indices.any { isSafe(report.removeAt(it)) }
            }
        }
    }
}
