import utils.filterNotBlank
import utils.part1
import utils.part2
import utils.readResourceAsString

fun solve(startingNumbers: List<Int>, numTurns: Int): Int {
    val spokenAt = mutableMapOf<Int, Int>()

    return (1..numTurns).fold(0) { lastSpoken, it ->
        val lastTurn = it - 1
        val lastSpokenAtTurn = spokenAt[lastSpoken]
        spokenAt[lastSpoken] = lastTurn
        val spoken =
            when {
                it - 1 < startingNumbers.size -> startingNumbers[it - 1]
                lastSpokenAtTurn != null -> lastTurn - lastSpokenAtTurn
                else -> 0
            }

        spoken
    }
}

fun main() {
    val input = readResourceAsString("/day15.txt")
    val startingNumbers = input.split(',').filterNotBlank().map { it.toInt() }

    part1 { solve(startingNumbers, 2020) }
    part2 { solve(startingNumbers, 30000000) }
}
