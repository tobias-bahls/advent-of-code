import datastructures.Grid
import datastructures.Point2D
import datastructures.Tile
import datastructures.parseGrid
import utils.*

private data class MirrorResult(val rowResult: Int?, val colResult: Int?) {
    val score =
        when {
            colResult != null -> colResult + 1
            rowResult != null -> (rowResult + 1) * 100
            else -> null
        }
}

private fun scoreGrid(
    grid: Grid<Boolean>,
    denyRows: Set<Int> = emptySet(),
    denyCols: Set<Int> = emptySet()
): MirrorResult {
    fun findPotentialMirrorPoints(rowsOrCols: List<List<Tile<Boolean>>>): List<Int> {
        return rowsOrCols
            .zipWithIndex()
            .windowed(2)
            .filter { (a, b) ->
                val aData = a.elem.map { it.data }
                val bData = b.elem.map { it.data }

                aData == bData
            }
            .map { it.first().index }
    }

    val columnMatchCandidates = findPotentialMirrorPoints(grid.columns()).filter { it !in denyCols }
    val colResult =
        columnMatchCandidates.singleOrNull { column ->
            val leftOfColumn = (0..column - 1).reversed()
            val rightOfColumn = (column + 2 ..< grid.width)

            leftOfColumn.zip(rightOfColumn).all { (a, b) ->
                val aData = grid.column(a).map { it.data }
                val bData = grid.column(b).map { it.data }

                aData == bData
            }
        }

    val rowMatchCandidates = findPotentialMirrorPoints(grid.rows()).filter { it !in denyRows }
    val rowResult =
        rowMatchCandidates.singleOrNull { row ->
            val leftOfRow = (0..row - 1).reversed()
            val rightOfRow = (row + 2 ..< grid.height)

            leftOfRow.zip(rightOfRow).all { (a, b) ->
                val aData = grid.row(a).map { it.data }
                val bData = grid.row(b).map { it.data }

                aData == bData
            }
        }

    return MirrorResult(rowResult, colResult)
}

fun main() {
    part1 {
        val input = readResourceAsString("/day13.txt")

        input
            .split("\n\n")
            .map { str -> parseGrid(str) { it == '#' } }
            .sumOf { scoreGrid(it).score ?: error("no score for grid") }
    }

    part2 {
        val input = readResourceAsString("/day13.txt")

        fun fixSmudges(grid: Grid<Boolean>): Sequence<Grid<Boolean>> {
            return grid.xRangeProgression.asSequence().flatMap { x ->
                grid.yRangeProgression.asSequence().map { y ->
                    val point = Point2D(x, y)

                    Grid(
                        grid.tiles.map {
                            if (it.point == point) it.copy(data = !it.data) else it.copy()
                        })
                }
            }
        }

        input
            .split("\n\n")
            .map { str -> parseGrid(str) { it == '#' } }
            .sumOf { grid ->
                val invalidResult = scoreGrid(grid)
                val denyRows = setOfNotNull(invalidResult.rowResult)
                val denyCols = setOfNotNull(invalidResult.colResult)

                fixSmudges(grid)
                    .map { scoreGrid(it, denyRows, denyCols) }
                    .firstOrNull { it.score != null }
                    ?.score!!
            }
    }
}
