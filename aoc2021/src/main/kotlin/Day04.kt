import utils.filterNotBlank
import utils.part1
import utils.part2
import utils.readResourceAsString

data class Board(
    private val board: List<List<Int>>,
) {
    private val rows: List<List<Int>>
        get() = board

    private val columns: List<List<Int>>
        get() = (0 until board.first().size).map { columnIndex -> board.map { it[columnIndex] } }

    fun isWinner(numbers: List<Int>): Boolean {
        return (rows + columns).any { rc -> rc.all { numbers.contains(it) } }
    }

    fun getUnmarked(numbers: List<Int>): List<Int> {
        return board.flatMap { row -> row.filter { !numbers.contains(it) } }
    }
}

fun main() {
    val (numbers, boards) =
        readResourceAsString("/day04.txt").lines().filterNotBlank().let { lines ->
            val numbers = lines.first().split(",").map { Integer.parseInt(it) }
            val boards =
                lines.drop(1).windowed(5, 5).map { board ->
                    Board(
                        board.map { row ->
                            row.split(" ").filter { it.isNotBlank() }.map { Integer.parseInt(it) }
                        })
                }

            Pair(numbers, boards)
        }

    part1 {
        numbers
            .mapIndexed { index, _ -> numbers.subList(0, index) }
            .firstNotNullOf { round ->
                boards
                    .find { it.isWinner(round) }
                    ?.let { Pair(round, it) }
                    ?.let { (round, board) -> board.getUnmarked(round).sum() * round.last() }
            }
    }

    part2 {
        numbers
            .mapIndexed { index, _ -> numbers.subList(0, index) }
            .fold(listOf()) { winners: List<Pair<List<Int>, Board>>, round ->
                val thisRoundWinners =
                    boards
                        .filter { it.isWinner(round) }
                        .filter { winner -> !winners.map { it.second }.contains(winner) }
                        .map { winner -> Pair(round, winner) }

                winners + thisRoundWinners
            }
            .last()
            .let { (round, board) -> board.getUnmarked(round).sum() * round.last() }
    }
}
