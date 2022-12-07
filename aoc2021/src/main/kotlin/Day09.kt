import datastructures.Grid
import datastructures.Tile
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.visitAllNodes

@JvmInline value class Height(val height: Int)

class Heightmap(private val grid: Grid<Height>) {

    fun lowPoints(): List<Tile<Height>> =
        grid.tiles.filter { tile ->
            tile.adjacentOrthogonally().all { neighbour ->
                tile.data.height < neighbour.data.height
            }
        }
}

fun main() {
    val input = readResourceAsString("/day09.txt")
    val heightmap = Heightmap(Grid(input) { Height(it.digitToInt()) })

    part1 { heightmap.lowPoints().sumOf { it.data.height + 1 } }

    part2 {
        heightmap
            .lowPoints()
            .map { lowPoint ->
                visitAllNodes(listOf(lowPoint)) {
                        it.adjacentOrthogonally().filter { adj -> adj.data.height != 9 }
                    }
                    .size
            }
            .sortedDescending()
            .take(3)
            .reduce(Int::times)
    }
}
