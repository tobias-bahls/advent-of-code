import datastructures.CardinalDirection.CardinalDirectionOrthogonal
import datastructures.CardinalDirection.East
import datastructures.CardinalDirection.South
import datastructures.Grid
import datastructures.Point2D
import datastructures.Tile
import datastructures.parseGrid
import utils.part1
import utils.readResourceAsString

private data class SeaCucumber(val direction: CardinalDirectionOrthogonal)

private fun step(grid: Grid<SeaCucumber>): Grid<SeaCucumber> {
    val eastCucumbers = grid.tiles.filter { it.data.direction == East }
    val newEastCucumbers = calculateNewTiles(eastCucumbers, grid)

    val southCucumbers = grid.tiles.filter { it.data.direction == South }
    val intermediateGrid = Grid(southCucumbers + newEastCucumbers, grid.width, grid.height)
    val newSouthCucumbers = calculateNewTiles(southCucumbers, intermediateGrid)

    return Grid(newEastCucumbers + newSouthCucumbers, grid.width, grid.height)
}

private fun calculateNewTiles(cucumbers: List<Tile<SeaCucumber>>, grid: Grid<SeaCucumber>) =
    cucumbers
        .groupBy {
            val targetPoint =
                it.pointInCardinalDirection(it.data.direction).let { p ->
                    Point2D(p.x % grid.width, p.y % grid.height)
                }
            if (grid.tileAt(targetPoint) == null) {
                targetPoint
            } else {
                it.point
            }
        }
        .mapValues { it.value.first() }
        .map { (k, v) -> Tile(k, v.data) }

fun main() {
    val input = readResourceAsString("/day25.txt")
    val grid =
        parseGrid(input) { char ->
            when (char) {
                '>' -> East
                'v' -> South
                else -> null
            }?.let { SeaCucumber(it) }
        }

    part1 { grid.transformUntilStable { step(it) }.iterations }
}
