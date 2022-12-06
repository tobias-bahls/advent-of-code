import Result.DRAW
import Result.LOSS
import Result.WIN
import utils.expectSize
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString

enum class Result {
    WIN,
    LOSS,
    DRAW;

    fun score(): Int =
        when (this) {
            WIN -> 6
            LOSS -> 0
            DRAW -> 3
        }

    companion object {
        fun fromChar(char: Char): Result {
            return when (char) {
                'X' -> LOSS
                'Y' -> DRAW
                'Z' -> WIN
                else -> error("Unknown char: $char")
            }
        }
    }
}

enum class Choice {
    ROCK,
    PAPER,
    SCISSORS;

    companion object {
        fun fromChar(char: Char): Choice {
            return when (char) {
                'A',
                'X' -> ROCK
                'B',
                'Y' -> PAPER
                'C',
                'Z' -> SCISSORS
                else -> error("Unknown char: $char")
            }
        }
    }
    fun score() =
        when (this) {
            ROCK -> 1
            PAPER -> 2
            SCISSORS -> 3
        }

    fun result(other: Choice): Result =
        when (this) {
            ROCK ->
                when (other) {
                    ROCK -> DRAW
                    PAPER -> LOSS
                    SCISSORS -> WIN
                }
            PAPER ->
                when (other) {
                    ROCK -> WIN
                    PAPER -> DRAW
                    SCISSORS -> LOSS
                }
            SCISSORS ->
                when (other) {
                    ROCK -> LOSS
                    PAPER -> WIN
                    SCISSORS -> DRAW
                }
        }

    fun outcome(result: Result): Choice =
        when (this) {
            ROCK ->
                when (result) {
                    DRAW -> ROCK
                    WIN -> PAPER
                    LOSS -> SCISSORS
                }
            PAPER ->
                when (result) {
                    DRAW -> PAPER
                    WIN -> SCISSORS
                    LOSS -> ROCK
                }
            SCISSORS ->
                when (result) {
                    DRAW -> SCISSORS
                    WIN -> ROCK
                    LOSS -> PAPER
                }
        }
}

data class Round(val theirs: Choice, val mine: Choice) {
    companion object {
        fun parse(input: String): Round {
            val (theirs, mine) = input.split(" ").expectSize(2).map { Choice.fromChar(it[0]) }

            return Round(theirs, mine)
        }
    }
}

data class RoundPart2(val theirs: Choice, val result: Result) {
    companion object {
        fun parse(input: String): RoundPart2 {
            val (theirs, result) = input.split(" ").expectSize(2).map { it[0] }

            return RoundPart2(Choice.fromChar(theirs), Result.fromChar(result))
        }
    }
}

fun main() {
    val fileContents = readResourceAsString("/day02.txt")

    part1 {
        fileContents
            .parseLines { Round.parse(it) }
            .sumOf { it.mine.score() + it.mine.result(it.theirs).score() }
    }

    part2 {
        fileContents
            .parseLines { RoundPart2.parse(it) }
            .sumOf { it.theirs.outcome(it.result).score() + it.result.score() }
    }
}
