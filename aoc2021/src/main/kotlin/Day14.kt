import utils.increment
import utils.match
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.reduceTimes
import utils.toPair
import utils.twoParts

class ReplacementRuleSet(rules: List<ReplacementRule>) {
    private val rulesMap: Map<String, String> = rules.associate { it.pair to it.insert }

    fun getReplacement(pair: String): String? = rulesMap[pair]
}

data class ReplacementRule(val pair: String, val insert: String)

fun parseReplacementRule(input: String) =
    input.match("""(\w+) -> (\w)""").toPair().let { (pair, insert) ->
        ReplacementRule(pair, insert)
    }

fun applyReplacementRules(
    rules: ReplacementRuleSet,
    previousOccurrences: Map<String, Long>
): MutableMap<String, Long> {
    val newOccurrences = mutableMapOf<String, Long>()
    previousOccurrences.forEach { (pair, count) ->
        val polymer = rules.getReplacement(pair)

        if (polymer != null) {
            newOccurrences.increment(pair[0] + polymer, count)
            newOccurrences.increment(polymer + pair[1], count)
        } else {
            newOccurrences.increment(pair, count)
        }
    }

    return newOccurrences
}

fun solve(steps: Int, template: String, rules: ReplacementRuleSet): Long {
    val initial = mutableMapOf<String, Long>()
    template.windowed(2).forEach { initial.increment(it) }

    val solution = reduceTimes(steps, initial) { applyReplacementRules(rules, it) }

    val mapped =
        solution
            .flatMap { (key, value) -> listOf(key[0] to value, key[1] to value) }
            .groupingBy { it.first }
            .fold(0L) { acc, elem -> acc + elem.second }
            .mapValues { (_, value) -> value / 2 }
            .toMutableMap()

    mapped.increment(template.first())
    mapped.increment(template.last())

    val min = mapped.map { it.value }.min()
    val max = mapped.map { it.value }.max()

    return max - min
}

fun main() {
    val input = readResourceAsString("/day14.txt")

    val (template, rawRules) = input.twoParts("\n\n")
    val rules = ReplacementRuleSet(rawRules.parseLines { parseReplacementRule(it) })

    part1 { solve(10, template, rules) }

    part2 { solve(40, template, rules) }
}
