import utils.*

private data class Day04Card(val id: Int, val winningNumbers: Set<Int>, val myNumbers: Set<Int>) {
    val matchingNumbers = (winningNumbers intersect myNumbers).size
    val score = 2.pow(matchingNumbers - 1)
}

private fun parseNumberSet(numbers: String) =
    numbers.split(" ").filterNotBlank().map { it.toInt() }.toSet()

private fun parseCard(line: String) =
    line.match("Card\\s* (\\d+): (.*) \\| (.*)").let { (id, winning, mine) ->
        Day04Card(id.toInt(), parseNumberSet(winning), parseNumberSet(mine))
    }

fun main() {
    part1 {
        val input = readResourceAsString("/day04.txt")

        val cards = input.parseLines { parseCard(it) }

        cards.sumOf { it.score }
    }

    part2 {
        val input = readResourceAsString("/day04.txt")
        val cards = input.parseLines { parseCard(it) }

        val counter = Counter<Day04Card>()
        cards.forEach { card ->
            counter.increment(card)

            val score = card.matchingNumbers
            cards
                .dropWhile { it != card }
                .drop(1)
                .take(score)
                .forEach { counter.incrementBy(it, counter[card]) }
        }

        counter.counts.values.sum()
    }
}
