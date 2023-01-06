import utils.filterNotBlank
import utils.part1
import utils.part2
import utils.readResourceAsString

private data class Individual(val answers: Set<Char>)

private data class Group(val individuals: List<Individual>) {
    val distinctAnswers
        get() = individuals.flatMap { it.answers }.distinct()

    val allYesAnswers
        get() = individuals.map { it.answers }.reduce { acc, it -> acc.intersect(it) }
}

private fun parseGroup(rawInput: String) =
    Group(rawInput.lines().map { Individual(it.toCharArray().toSet()) })

fun main() {
    val input = readResourceAsString("/day06.txt")
    val groups = input.split("\n\n").filterNotBlank().map { parseGroup(it) }

    part1 { groups.sumOf { it.distinctAnswers.size } }
    part2 { groups.sumOf { it.allYesAnswers.size } }
}
