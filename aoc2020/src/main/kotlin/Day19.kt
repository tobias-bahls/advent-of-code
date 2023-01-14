import utils.*

@JvmInline
private value class MessageRuleId(val id: String) {
    fun addTick() = MessageRuleId("$id'")
}

private sealed interface MessageRule {
    val lhs: MessageRuleId

    fun copyWithRuleId(newRuleId: MessageRuleId): MessageRule

    data class Unit(override val lhs: MessageRuleId, val unit: MessageRuleId) : MessageRule {
        override fun copyWithRuleId(newRuleId: MessageRuleId) = copy(lhs = newRuleId)
    }

    data class Production(
        override val lhs: MessageRuleId,
        val b: MessageRuleId,
        val c: MessageRuleId
    ) : MessageRule {
        override fun copyWithRuleId(newRuleId: MessageRuleId) = copy(lhs = newRuleId)
    }

    data class Literal(override val lhs: MessageRuleId, val literal: Char) : MessageRule {
        override fun copyWithRuleId(newRuleId: MessageRuleId) = copy(lhs = newRuleId)
    }
}

private fun parseAndConvertMessageRule(raw: String): List<MessageRule> {
    val (ruleId, rawRule) = raw.match("""(\d+): (.*)""").toPair().mapFirst { MessageRuleId(it) }

    return when {
        rawRule.contains("\"") -> listOf(MessageRule.Literal(ruleId, rawRule[1]))
        rawRule.contains("|") -> rawRule.split(" | ").flatMap { createProductionRules(ruleId, it) }
        else -> createProductionRules(ruleId, rawRule)
    }
}

private fun createProductionRules(ruleId: MessageRuleId, rawProductions: String) =
    createProductionRules(ruleId, rawProductions.split(" ").map { MessageRuleId(it) })

private fun createProductionRules(
    ruleId: MessageRuleId,
    productions: List<MessageRuleId>
): List<MessageRule> {
    return when (productions.size) {
        1 -> listOf(MessageRule.Unit(ruleId, productions.single()))
        2 -> {
            val (b, c) = productions
            listOf(MessageRule.Production(ruleId, b, c))
        }
        else -> {
            val a = productions.first()
            val rest = productions.tail()
            val newRuleId = ruleId.addTick()

            listOf(MessageRule.Production(ruleId, a, newRuleId)) +
                createProductionRules(newRuleId, rest)
        }
    }
}

private class MessageRuleSet(val rules: Set<MessageRule>) {
    val literalProductions = rules.filterIsInstance<MessageRule.Literal>()
    val productions = rules.filterIsInstance<MessageRule.Production>()

    fun eliminateUnitRules(): MessageRuleSet {
        val unitRules = rules.filterIsInstance<MessageRule.Unit>().toSet()
        if (unitRules.isEmpty()) {
            return this
        }

        val newRules =
            unitRules
                .flatMap { rule ->
                    rules
                        .filter { it.lhs == rule.unit }
                        .map { resolved -> resolved.copyWithRuleId(rule.lhs) }
                }
                .toSet()

        return MessageRuleSet(rules - unitRules + newRules)
    }

    fun matches(string: String): Boolean {
        // https://en.wikipedia.org/wiki/CYK_algorithm
        val n = string.length
        val dp = Array(n + 1) { Array(n + 1) { mutableMapOf<MessageRuleId, Boolean>() } }

        val dp1 = dp[1]
        string.forEachIndexed { index, char ->
            literalProductions.forEach {
                if (it.literal != char) return@forEach

                dp1[index + 1][it.lhs] = true
            }
        }

        (2..n).forEach { l ->
            (1..n - l + 1).forEach { s ->
                val ls = dp[l][s]

                (1 until l).forEach { p ->
                    val ps = dp[p][s]
                    val lpsp = dp[l - p][s + p]

                    productions.forEach productionsForEach@{ prod ->
                        ps[prod.b] ?: return@productionsForEach
                        lpsp[prod.c] ?: return@productionsForEach

                        ls[prod.lhs] = true
                    }
                }
            }
        }

        return dp[n][1][MessageRuleId("0")] ?: false
    }
}

private fun parseRuleSet(rawRules: String): MessageRuleSet {
    val rules = rawRules.parseLines { parseAndConvertMessageRule(it) }.flatten()
    return MessageRuleSet(rules.toSet()).eliminateUnitRules()
}

fun main() {
    val input = readResourceAsString("/day19.txt")

    part1 {
        val (rawRules, rawMessages) = input.split("\n\n")
        val ruleSet = parseRuleSet(rawRules)

        rawMessages.lines().filterNotBlank().count { ruleSet.matches(it) }
    }
    part2 {
        val newInput =
            input.replace("8: 42", "8: 42 | 42 8").replace("11: 42 31", "11: 42 31 | 42 11 31")
        val (rawRules, rawMessages) = newInput.split("\n\n")
        val ruleSet = parseRuleSet(rawRules)

        rawMessages.lines().filterNotBlank().count { ruleSet.matches(it) }
    }
}
