import utils.*

data class OrderingRule(val a: Long, val b: Long) {
    fun matches(report: List<Long>): Boolean {
        val aIndexes = report.zipWithIndex().filter { (p, _) -> p == a }.map { it.index }
        val bIndexes = report.zipWithIndex().filter { (p, _) -> p == b }.map { it.index }

        return aIndexes.all { aIndex -> bIndexes.all { it > aIndex } }
    }
}

fun parseOrderingRule(str: String): OrderingRule {
    val (a, b) = str.twoParts("|").map { it.toLong() }

    return OrderingRule(a, b)
}

fun parseReport(str: String): List<Long> {
    return str.split(",").map { it.toLong() }
}

fun main() {
    part1 {
        val input = readResourceAsString("/day05.txt")
        val (rules, reports) =
            input
                .twoParts("\n\n")
                .mapFirst { f -> f.parseLines { parseOrderingRule(it) } }
                .mapSecond { f -> f.parseLines { parseReport(it) } }

        reports.sumOf { report ->
            val correctOrder = rules.all { it.matches(report) }

            if (correctOrder) {
                report.middleElement()
            } else {
                0
            }
        }
    }

    part2 {
        val input = readResourceAsString("/day05.txt")
        val (rules, reports) =
            input
                .twoParts("\n\n")
                .mapFirst { f -> f.parseLines { parseOrderingRule(it) } }
                .mapSecond { f -> f.parseLines { parseReport(it) } }

        reports.sumOf { report ->
            val incorrectOrder = rules.any { !it.matches(report) }

            if (incorrectOrder) {
                report
                    .sortedWith { a, b ->
                        val aRules = rules.filter { it.a == a }
                        val isBefore = aRules.any { it.b == b }

                        if (isBefore) {
                            -1
                        } else {
                            0
                        }
                    }
                    .middleElement()
            } else {
                0
            }
        }
    }
}
