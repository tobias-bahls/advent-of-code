import utils.filterNotBlank
import utils.match
import utils.parseClosedIntRange
import utils.part1
import utils.part2
import utils.readResourceAsString

private data class Rule(val name: String, val ranges: List<IntRange>) {
    fun isValid(value: Int) = ranges.any { value in it }
}

private data class RuleSet(val rules: List<Rule>) {
    fun isValid(value: Int) = rules.any { it.isValid(value) }

    fun isValid(ticket: Ticket) = ticket.fields.all { isValid(it) }
}

private fun parseRule(input: String): Rule {
    val (name, range1, range2) = input.match("""(.*): (.*) or (.*)""")

    return Rule(name, listOf(parseClosedIntRange(range1), parseClosedIntRange(range2)))
}

private data class Ticket(val fields: List<Int>)

private fun parseTicket(input: String) = input.split(",").map { it.toInt() }.let { Ticket(it) }

fun main() {
    val input = readResourceAsString("/day16.txt")

    val (rawRules, rawMyTicket, rawNearbyTicket) = input.split("\n\n")

    val rules = rawRules.lines().filterNotBlank().map { parseRule(it) }
    val myTicket = rawMyTicket.lines().filterNotBlank().drop(1).map { parseTicket(it) }.single()
    val nearbyTickets = rawNearbyTicket.lines().filterNotBlank().drop(1).map { parseTicket(it) }

    val ruleSet = RuleSet(rules)
    part1 { nearbyTickets.flatMap { it.fields }.filter { !ruleSet.isValid(it) }.sum() }
    part2 {
        val ticketFieldsToRules =
            nearbyTickets
                .filter { ruleSet.isValid(it) }
                .flatMap { ticket -> ticket.fields.withIndex() }
                .groupBy { it.index }
                .mapValues { (_, v) -> v.map { it.value } }
                .mapValues { (_, v) -> rules.filter { rule -> v.all { rule.isValid(it) } } }

        fun solve(ruleMap: Map<Int, Rule>, currentField: Int): Map<Int, Rule> {
            val usedRules = ruleMap.values.toSet()
            if (usedRules.size == rules.size) {
                return emptyMap()
            }

            if (currentField == myTicket.fields.size - 1) {
                return ruleMap
            }

            return ticketFieldsToRules[currentField]!!
                .filter { it !in usedRules }
                .map { rule -> solve(ruleMap + (currentField to rule), currentField + 1) }
                .find { it.isNotEmpty() }
                ?: emptyMap()
        }

        val result = solve(mapOf(), 0)

        result
            .filter { it.value.name.startsWith("departure") }
            .map { myTicket.fields[it.key].toLong() }
            .reduce(Long::times)
    }
}
