import Day03Tile.*
import datastructures.Tile
import datastructures.parseGrid
import utils.chunksSatisfying
import utils.part1
import utils.part2
import utils.readResourceAsString

private sealed interface Day03Tile {
    data object Floor : Day03Tile

    data class Digit(val digit: Int) : Day03Tile

    data class Symbol(val symbol: Char) : Day03Tile
}

private data class PartNumber(val tiles: List<Tile<Day03Tile>>) {
    val number = tiles.joinToString("") { (it.data as Digit).digit.toString() }.toInt()
    val adjacentTiles = tiles.flatMap { it.adjacent() }
}

fun main() {
    val input = readResourceAsString("/day03.txt")
    val grid =
        parseGrid(input) {
            when {
                it == '.' -> Floor
                it.isDigit() -> Digit(it.digitToInt())
                else -> Symbol(it)
            }
        }

    fun findPartNumbers(): List<PartNumber> {
        val partNumbers =
            grid.yRangeProgression.flatMap { y ->
                grid.row(y).chunksSatisfying { it.data is Digit }.map { chunk -> PartNumber(chunk) }
            }
        return partNumbers
    }

    part1 {
        val partNumbers = findPartNumbers()

        partNumbers
            .filter { number -> number.adjacentTiles.any { it.data is Symbol } }
            .sumOf { it.number }
    }

    part2 {
        val partNumbers = findPartNumbers()

        grid.tiles
            .filter { it.data is Symbol && (it.data as Symbol).symbol == '*' }
            .mapNotNull { potentialGear ->
                val adjacentPartNumbers = partNumbers.filter { potentialGear in it.adjacentTiles }

                if (adjacentPartNumbers.size != 2) {
                    null
                } else {
                    adjacentPartNumbers.map { it.number }.reduce { a, b -> a * b }
                }
            }
            .sum()
    }
}
