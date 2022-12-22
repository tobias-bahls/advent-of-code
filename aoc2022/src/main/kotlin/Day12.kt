import datastructures.Tile
import datastructures.parseGrid
import utils.Scored
import utils.part1
import utils.part2
import utils.readResourceAsString

@JvmInline
private value class TileData(val char: Char) {
    val height
        get() =
            when (char) {
                'S' -> heightOf('a')
                'E' -> heightOf('z')
                in ('a'..'z') -> heightOf(char)
                else -> error("Unknown char: $char")
            }
    val start
        get() = char == 'S'
    val end
        get() = char == 'E'

    private fun heightOf(char: Char) = char.code - 'a'.code
}

private fun determineNeighbours(tile: Tile<TileData>) =
    tile
        .adjacentOrthogonally()
        .filter { neighbour -> neighbour.data.height - tile.data.height <= 1 }
        .map { Scored(1, it) }

fun main() {
    val input = readResourceAsString("/day12.txt")
    val grid = parseGrid(input) { TileData(it) }

    val endNode = grid.tiles.find { it.data.end } ?: error("Could not find end node")
    part1 {
        val start = grid.tiles.find { it.data.start } ?: error("Could not find start node")

        grid.dijkstra(start, endNode) { it -> determineNeighbours(it) }?.size?.minus(1)
            ?: error("Could not find shortest path")
    }

    part2 {
        val lowestTiles = grid.tiles.filter { it.data.height == 0 }

        lowestTiles.minOf { startNode ->
            grid.dijkstra(startNode, endNode) { it -> determineNeighbours(it) }?.size?.minus(1)
                ?: Integer.MAX_VALUE
        }
    }
}
