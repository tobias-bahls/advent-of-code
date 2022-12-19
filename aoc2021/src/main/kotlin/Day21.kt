import utils.cartesian
import utils.oneBasedModulo
import utils.part1
import utils.part2

class DeterministicDice {
    private var current: Int = 0
    var totalRolls: Int = 0

    fun rollThrice(): Int = (0 until 3).map { roll() }.reduce(Int::plus)

    private fun roll(): Int {
        current = (current + 1).oneBasedModulo(100)
        totalRolls++
        return current
    }
}

data class Game(
    val positions: Map<Int, Int>,
    val scores: Map<Int, Int> = positions.mapValues { 0 },
    val currentPlayer: Int = 1
) {
    val highestScore: Int
        get() = scores.maxOf { it.value }

    val winner: Int
        get() = scores.maxBy { it.value }.key

    val losingEntry: Pair<Int, Int>
        get() = scores.minBy { it.value }.toPair()

    fun move(steps: Int): Game {
        val newPosition = (positions.getValue(currentPlayer) + steps).oneBasedModulo(10)
        val newScore = scores.getValue(currentPlayer) + newPosition
        val nextPlayer =
            if (currentPlayer == 1) {
                2
            } else {
                1
            }

        return copy(
            positions = positions + (currentPlayer to newPosition),
            scores = scores + (currentPlayer to newScore),
            currentPlayer = nextPlayer)
    }
}

fun main() {
    val game = Game(mapOf(1 to 4, 2 to 7))

    part1 {
        val dice = DeterministicDice()
        var latestGame = game
        while (true) {
            if (latestGame.highestScore >= 1000) {
                break
            }

            latestGame = latestGame.move(dice.rollThrice())
        }

        val (_, losingScore) = latestGame.losingEntry

        losingScore * dice.totalRolls
    }

    part2 {
        val diracRolls =
            listOf(1, 2, 3)
                .cartesian(listOf(1, 2, 3))
                .cartesian(listOf(1, 2, 3))
                .map { (p, c) ->
                    val (a, b) = p

                    a + b + c
                }
                .toList()

        data class Result(val player1Wins: Long, val player2Wins: Long) {
            operator fun plus(other: Result) =
                Result(this.player1Wins + other.player1Wins, this.player2Wins + other.player2Wins)
        }

        val memo = mutableMapOf<Game, Result>()
        fun countWins(game: Game): Result {
            if (game in memo) {
                return memo.getValue(game)
            }
            if (game.highestScore >= 21) {
                return if (game.winner == 1) {
                    Result(1, 0)
                } else {
                    Result(0, 1)
                }
            }

            val result = diracRolls.map { countWins(game.move(it)) }.reduce(Result::plus)

            memo[game] = result
            return result
        }

        val result = countWins(game)

        result.player1Wins.coerceAtLeast(result.player2Wins)
    }
}
