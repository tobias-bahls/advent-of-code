import datastructures.Tile
import datastructures.parseGrid
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.visitAllNodes

fun walkTiles(tile: Tile<Int>, block: (Tile<Int>) -> Tile<Int>?) =
    visitAllNodes(listOf(tile)) { t -> block(t)?.let { listOf(it) } ?: listOf() }

fun tilesAlong(tile: Tile<Int>, block: (Tile<Int>) -> Tile<Int>?) =
    walkTiles(tile) { block(it) }.drop(1)

fun visibleAlong(tile: Tile<Int>, block: (Tile<Int>) -> Tile<Int>?) =
    tilesAlong(tile, block).all { it.data < tile.data }

fun viewingDistance(tile: Tile<Int>, block: (Tile<Int>) -> Tile<Int>?): Int {
    val tiles = tilesAlong(tile, block)
    val blockingTreeIdx = tiles.indexOfFirst { it.data >= tile.data }

    return if (blockingTreeIdx == -1) {
        tiles.size
    } else {
        blockingTreeIdx + 1
    }
}

fun main() {
    val input = readResourceAsString("/day08.txt")
    val grid = parseGrid(input) { it.digitToInt() }

    part1 {
        grid.tiles
            .filter { tile ->
                when {
                    tile.isEdge() -> true
                    visibleAlong(tile) { it.above } -> true
                    visibleAlong(tile) { it.right } -> true
                    visibleAlong(tile) { it.below } -> true
                    visibleAlong(tile) { it.left } -> true
                    else -> false
                }
            }
            .size
    }

    part2 {
        grid.tiles.maxOf { tile ->
            viewingDistance(tile) { it.above } *
                viewingDistance(tile) { it.right } *
                viewingDistance(tile) { it.below } *
                viewingDistance(tile) { it.left }
        }
    }
}
