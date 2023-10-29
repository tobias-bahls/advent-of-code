import utils.filterNotBlank
import utils.match
import utils.part1
import utils.part2
import utils.readResourceAsString

private data class Player(val id: Int, val deck: List<Int>) {
    val deckSize = deck.size

    val lostGame = deck.isEmpty()
    val topCard
        get() = deck.first()

    fun loseTopmostCard() = copy(deck = deck.drop(1))

    fun copyDeckForSubGame() = copy(deck = deck.drop(1).take(topCard))

    fun winCard(otherCard: Int) = copy(deck = deck.drop(1) + listOf(deck.first(), otherCard))
}

private fun parsePlayer(input: String): Player {
    val lines = input.lines().filterNotBlank()
    val (playerId) = input.lines().first().match("""Player (\d+):""")
    val deck = lines.drop(1).map { it.toInt() }

    return Player(playerId.toInt(), deck)
}

private fun playPart1(player1: Player, player2: Player): Player {
    var p1 = player1
    var p2 = player2

    while (true) {
        if (p1.topCard > p2.topCard) {
            p1 = p1.winCard(p2.topCard)
            p2 = p2.loseTopmostCard()
        } else {
            p2 = p2.winCard(p1.topCard)
            p1 = p1.loseTopmostCard()
        }

        when {
            p1.lostGame -> return p2
            p2.lostGame -> return p1
        }
    }
}

private data class Round(val player1: Player, val player2: Player)

private val memo = mutableMapOf<Round, Player>()

private fun playPart2(player1: Player, player2: Player): Player {
    val memoKey = Round(player1, player2)
    val memoResult = memo[memoKey]
    if (memoResult != null) {
        return memoResult
    }

    var p1 = player1
    var p2 = player2

    val seenRounds = mutableSetOf<Round>()
    while (true) {
        val round = Round(p1, p2)
        if (round in seenRounds) {
            memo[memoKey] = p1
            return p1
        }
        seenRounds += round

        val winner =
            if (p1.deckSize - 1 >= p1.topCard && p2.deckSize - 1 >= p2.topCard) {
                val winner = playPart2(p1.copyDeckForSubGame(), p2.copyDeckForSubGame())

                listOf(p1, p2).find { it.id == winner.id }!!
            } else {
                if (p1.topCard > p2.topCard) p1 else p2
            }

        val loser = listOf(p1, p2).find { it.id != winner.id }!!

        val newPlayers = listOf(winner.winCard(loser.topCard), loser.loseTopmostCard())

        p1 = newPlayers.find { it.id == p1.id }!!
        p2 = newPlayers.find { it.id == p2.id }!!

        when {
            p1.lostGame -> p2
            p2.lostGame -> p1
            else -> null
        }?.also {
            memo[memoKey] = it
            return it
        }
    }
}

private fun calculateScore(winningDeck: List<Int>) =
    winningDeck.zip(winningDeck.size downTo 1).sumOf { (a, b) -> a * b }

fun main() {
    val input = readResourceAsString("/day22.txt")
    val (player1, player2) = input.split("\n\n").map { parsePlayer(it) }

    part1 {
        val winningDeck = playPart1(player1, player2).deck
        calculateScore(winningDeck)
    }

    part2 {
        val winningDeck = playPart2(player1, player2).deck
        calculateScore(winningDeck)
    }
}
