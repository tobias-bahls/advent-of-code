import Day02Color.*
import utils.*

private enum class Day02Color {
    RED,
    GREEN,
    BLUE
}

fun main() {
    data class Shown(val num: Int, val color: Day02Color)
    fun parseShown(str: String) =
        str.match("(\\d+) (.*)")
            .toPair()
            .mapFirst { it.toInt() }
            .mapSecond { it.toEnum<Day02Color>() }
            .let { (num, color) -> Shown(num, color) }

    data class Round(val shown: List<Shown>) {
        fun totalShown(color: Day02Color) = shown.filter { it.color == color }.sumOf { it.num }
    }
    fun parseRound(str: String) = Round(str.split(",").map { parseShown(it) })

    data class Game(val id: Int, val rounds: List<Round>)
    fun parseGame(str: String) =
        str.match("Game (\\d+): (.*)")
            .toPair()
            .mapFirst { it.toInt() }
            .mapSecond { rounds -> rounds.split(";").map { parseRound(it) } }
            .let { (id, rounds) -> Game(id, rounds) }

    part1 {
        val input = readResourceAsString("/day02.txt")

        val games = input.parseLines { line -> parseGame(line) }

        val maxRed = 12
        val maxGreen = 13
        val maxBlue = 14

        games
            .filter { game ->
                game.rounds.all {
                    it.totalShown(RED) <= maxRed &&
                        it.totalShown(GREEN) <= maxGreen &&
                        it.totalShown(BLUE) <= maxBlue
                }
            }
            .sumOf { it.id }
    }

    part2 {
        val input = readResourceAsString("/day02.txt")
        val games = input.parseLines { line -> parseGame(line) }

        games.sumOf { game ->
            val reds = game.rounds.maxOf { it.totalShown(RED) }
            val greens = game.rounds.maxOf { it.totalShown(GREEN) }
            val blues = game.rounds.maxOf { it.totalShown(BLUE) }

            reds * greens * blues
        }
    }
}
