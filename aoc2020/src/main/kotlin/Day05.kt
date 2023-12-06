import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString

private enum class Row {
    FRONT,
    BACK
}

private fun parseRow(input: Char): Row =
    when (input) {
        'F' -> Row.FRONT
        'B' -> Row.BACK
        else -> error("Unreachable: $input")
    }

private enum class Column {
    LEFT,
    RIGHT
}

private fun parseColumn(input: Char): Column =
    when (input) {
        'L' -> Column.LEFT
        'R' -> Column.RIGHT
        else -> error("Unreachable: $input")
    }

private data class BoardingPass(val rows: List<Row>, val columns: List<Column>) {
    val column by lazy { binarySpacePartition(0..7, columns, Column.LEFT, Column.RIGHT) }
    val row by lazy { binarySpacePartition(0..127, rows, Row.FRONT, Row.BACK) }

    val seatId by lazy { (row * 8) + column }

    private fun <T> binarySpacePartition(
        bounds: IntRange,
        elems: List<T>,
        lowerHalf: T,
        upperHalf: T
    ) =
        elems
            .fold(bounds) { acc, row ->
                val half = (acc.last - acc.first + 1) / 2
                when (row) {
                    lowerHalf -> acc.first..(acc.last - half)
                    upperHalf -> (acc.first + half)..acc.last
                    else -> error("Unreachable: $row")
                }
            }
            .toList()
            .singleOrNull() ?: error("Did not end up with single value")
}

private fun parseBoardingPass(input: String): BoardingPass {
    val rows = input.slice(0..6).map { parseRow(it) }
    val cols = input.slice(7..9).map { parseColumn(it) }

    return BoardingPass(rows, cols)
}

fun main() {
    val input = readResourceAsString("/day05.txt")
    val parsed = input.parseLines { parseBoardingPass(it) }

    part1 { parsed.maxOf { it.seatId } }

    part2 {
        parsed
            .asSequence()
            .map { it.seatId }
            .sorted()
            .windowed(2)
            .find { (a, b) -> a + 1 != b }
            ?.first()
            ?.plus(1) ?: error("Could not find missing boarding pass")
    }
}
