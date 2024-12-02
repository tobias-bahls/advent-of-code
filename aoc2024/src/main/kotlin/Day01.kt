import kotlin.math.absoluteValue
import utils.*

fun main() {
    part1 {
        val input = readResourceAsString("/day01.txt")
        val leftList = input.parseLines { it.split(" ").first().toInt() }.sorted()
        val rightList = input.parseLines { it.split(" ").last().toInt() }.sorted()

        leftList.zip(rightList).sumOf { (l, r) -> (l - r).absoluteValue }
    }

    part2 {
        val input = readResourceAsString("/day01.txt")
        val leftList = input.parseLines { it.split(" ").first().toInt() }
        val rightFrequencies = input.parseLines { it.split(" ").last().toInt() }.toFrequencyMap()

        leftList.sumOf { it * (rightFrequencies[it] ?: 0) }
    }
}
