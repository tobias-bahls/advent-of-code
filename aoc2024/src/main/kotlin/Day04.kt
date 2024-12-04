import datastructures.CardinalDirection
import datastructures.CardinalDirection.*
import datastructures.Point2D
import datastructures.Tile
import datastructures.parseGrid
import utils.part1
import utils.part2
import utils.readResourceAsString

fun main() {
    val sample =
        """
        MMMSXXMASM
        MSAMXMSMSA
        AMXSXMAAMM
        MSAMASMSMX
        XMASAMXAMM
        XXAMMXXAMA
        SMSMSASXSS
        SAXAMASAAA
        MAMMMXMMMM
        MXMXAXMASX
    """
            .trimIndent()

    part1 {
        val input = readResourceAsString("/day04.txt")
        val grid = parseGrid(input) { it }

        data class XmasMatch(
            val current: Tile<Char>,
            val soFar: String = "",
            val direction: CardinalDirection,
            val length: Int
        ) {
            fun hasNext() =
                !matches() &&
                    length < "XMAS".length &&
                    current.directNeighbourInDirection(direction) != null

            fun matches() = soFar == "XMAS"

            fun next(): XmasMatch {
                val neighbour = current.directNeighbourInDirection(direction)!!

                return XmasMatch(
                    current = neighbour,
                    soFar = soFar + neighbour.data,
                    direction = direction,
                    length = length + 1,
                )
            }
        }

        fun calculateMatch(initial: XmasMatch): Boolean {
            var current = initial
            while (current.hasNext()) {
                current = current.next()
            }

            return current.matches()
        }

        grid.yRangeProgression.sumOf { y ->
            grid.xRangeProgression.sumOf { x ->
                val point = Point2D(x, y)
                val currentTile = grid.tileAt(point) ?: error("No tile at $point")

                if (currentTile.data == 'X') {
                    CardinalDirection.ALL.count {
                        val neighbour = currentTile.directNeighbourInDirection(it)
                        if (neighbour != null) {
                            calculateMatch(XmasMatch(neighbour, "X" + neighbour.data, it, 1))
                        } else {
                            false
                        }
                    }
                } else {
                    0
                }
            }
        }
    }

    part2 {
        val input = readResourceAsString("/day04.txt")
        val grid = parseGrid(input) { it }

        data class MasMatch(
            val current: Tile<Char>,
            val soFar: String = "",
            val direction: CardinalDirection,
            val length: Int
        ) {
            fun hasNext() =
                !matches() &&
                    length < "MAS".length &&
                    current.directNeighbourInDirection(direction) != null

            fun matches() = soFar == "MAS"

            fun next(): MasMatch {
                val neighbour = current.directNeighbourInDirection(direction)!!

                return MasMatch(
                    current = neighbour,
                    soFar = soFar + neighbour.data,
                    direction = direction,
                    length = length + 1,
                )
            }
        }

        fun calculateMatch(initial: MasMatch): Boolean {
            var current = initial
            while (current.hasNext()) {
                current = current.next()
            }

            return current.matches()
        }

        grid.yRangeProgression.sumOf { y ->
            grid.xRangeProgression.sumOf { x ->
                val point = Point2D(x, y)
                val currentTile = grid.tileAt(point) ?: error("No tile at $point")

                if (currentTile.data == 'A') {
                    val dirs =
                        listOf(
                            NorthEast to SouthWest,
                            NorthWest to SouthEast,
                            SouthEast to NorthWest,
                            SouthWest to NorthEast)

                    val masMatches =
                        dirs.count { (start, dir) ->
                            val startMatch = currentTile.directNeighbourInDirection(start)

                            if (startMatch != null && startMatch.data == 'M') {
                                val neighbour = startMatch.directNeighbourInDirection(dir)
                                if (neighbour != null) {
                                    calculateMatch(
                                        MasMatch(neighbour, "M" + neighbour.data, dir, 1))
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        }

                    if (masMatches == 2) {
                        1L
                    } else {
                        0
                    }
                } else {
                    0
                }
            }
        }
    }
}
