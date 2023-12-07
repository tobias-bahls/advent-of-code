import Day07Value.*
import HandType.*
import utils.*

private enum class Day07Value : Comparable<Day07Value> {
    A,
    K,
    Q,
    J,
    T,
    NINE,
    EIGHT,
    SEVEN,
    SIX,
    FIVE,
    FOUR,
    THREE,
    TWO,
    ;

    val strength by lazy { entries.size - ordinal }

    companion object {
        val NON_JOKER = entries - J
    }
}

private fun parseDay07Value(char: Char) =
    when (char) {
        'A' -> A
        'K' -> K
        'Q' -> Q
        'J' -> J
        'T' -> T
        '9' -> NINE
        '8' -> EIGHT
        '7' -> SEVEN
        '6' -> SIX
        '5' -> FIVE
        '4' -> FOUR
        '3' -> THREE
        '2' -> TWO
        else -> unreachable("char $char")
    }

private enum class HandType {
    FIVE_OF_A_KIND,
    FOUR_OF_A_KIND,
    FULL_HOUSE,
    THREE_OF_A_KIND,
    TWO_PAIR,
    ONE_PAIR,
    HIGH_CARD,
    ;

    val strength by lazy { entries.size - ordinal }
}

private fun List<Day07Value>.compareHand(other: List<Day07Value>, by: (Day07Value) -> Int) =
    this.zip(other).map { (a, b) -> compareBy(by).compare(a, b) }.firstOrNull { it != 0 } ?: 0

private data class Day07Hand(val cards: List<Day07Value>) : Comparable<Day07Hand> {
    private val counts = cards.groupingBy { it }.eachCount().values.sortedDescending()

    val type =
        when (counts) {
            listOf(5) -> FIVE_OF_A_KIND
            listOf(4, 1) -> FOUR_OF_A_KIND
            listOf(3, 2) -> FULL_HOUSE
            listOf(3, 1, 1) -> THREE_OF_A_KIND
            listOf(2, 2, 1) -> TWO_PAIR
            listOf(2, 1, 1, 1) -> ONE_PAIR
            listOf(1, 1, 1, 1, 1) -> HIGH_CARD
            else -> unreachable("hand: $cards ($counts)")
        }

    override fun compareTo(other: Day07Hand): Int {
        val byType = this.type.strength.compareTo(other.type.strength)
        if (byType != 0) {
            return byType
        }

        return this.cards.compareHand(other.cards) { it.strength }
    }
}

private data class Day07HandPart2(val cards: List<Day07Value>) : Comparable<Day07HandPart2> {
    val type = replaceJokers(cards).map { Day07Hand(it).type }.maxBy { it.strength }

    private fun replaceJokers(cards: List<Day07Value>): List<List<Day07Value>> {
        val jokerIndex = cards.indexOfFirst { it == J }
        return if (jokerIndex == -1) {
            listOf(cards)
        } else {
            Day07Value.NON_JOKER.flatMap { replaceJokers(cards.updated(jokerIndex, it)) }
        }
    }

    override fun compareTo(other: Day07HandPart2): Int {
        val byType = this.type.strength.compareTo(other.type.strength)
        if (byType != 0) {
            return byType
        }

        return this.cards.compareHand(other.cards) { if (it == J) -1 else it.strength }
    }
}

private fun parseHand(hand: String) = Day07Hand(hand.map { parseDay07Value(it) })

private fun parseHandPart2(hand: String) = Day07HandPart2(hand.map { parseDay07Value(it) })

private data class Day07Game(val hand: Day07Hand, val bid: Int)

private fun parseDay07Game(str: String) =
    str.twoParts(" ")
        .mapFirst { parseHand(it) }
        .mapSecond { it.toInt() }
        .let { (hand, bid) -> Day07Game(hand, bid) }

private data class Day07GamePart2(val hand: Day07HandPart2, val bid: Int)

private fun parseDay07GamePart2(str: String) =
    str.twoParts(" ")
        .mapFirst { parseHandPart2(it) }
        .mapSecond { it.toInt() }
        .let { (hand, bid) -> Day07GamePart2(hand, bid) }

fun main() {
    part1 {
        val input = readResourceAsString("/day07.txt")

        input
            .parseLines { parseDay07Game(it) }
            .sortedBy { it.hand }
            .mapIndexed { index, game -> (index + 1) * game.bid }
            .sum()
    }

    part2 {
        val input = readResourceAsString("/day07.txt")

        input
            .parseLines { parseDay07GamePart2(it) }
            .sortedBy { it.hand }
            .mapIndexed { index, game -> (index + 1) * game.bid }
            .sum()
    }
}
