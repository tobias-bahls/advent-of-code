import datastructures.*
import utils.*

private fun Grid<Boolean>.addAndRemoveBugs(
    toAdd: List<Tile<Boolean>>,
    toRemove: List<Tile<Boolean>>
) =
    Grid(
        tiles.map {
            when (it) {
                in toAdd -> it.copy(data = true)
                in toRemove -> it.copy(data = false)
                else -> it
            }
        })

private fun stepPart1(grid: Grid<Boolean>): Grid<Boolean> {
    val remove =
        grid.tiles.filter { tile ->
            tile.data && tile.adjacentOrthogonally().count { it.data } != 1
        }

    val add =
        grid.tiles.filter { tile ->
            !tile.data && tile.adjacentOrthogonally().count { it.data } in listOf(1, 2)
        }

    return grid.addAndRemoveBugs(toAdd = add, toRemove = remove)
}

private fun stepPart2(recursiveGrids: RecursiveGrids) {
    val remove =
        recursiveGrids
            .allTiles()
            .filter { tile ->
                tile.tile.data && recursiveGrids.adjacent(tile).count { it.tile.data } != 1
            }
            .groupBy { it.depth }

    val add =
        recursiveGrids
            .allTiles()
            .filter { tile ->
                !tile.tile.data &&
                    recursiveGrids.adjacent(tile).count { it.tile.data } in listOf(1, 2)
            }
            .groupBy { it.depth }

    val modifiedDepths = remove.keys + add.keys

    modifiedDepths.forEach { depth ->
        recursiveGrids.updateDepth(depth) { grid ->
            grid.addAndRemoveBugs(
                toAdd = add[depth]?.map { it.tile } ?: emptyList(),
                toRemove = remove[depth]?.map { it.tile } ?: emptyList(),
            )
        }
    }
}

private fun biodiversityRating(grid: Grid<Boolean>) =
    grid.yRangeProgression.sumOf { y ->
        grid.xRangeProgression.sumOf { x ->
            if (grid.tileAt(Point2D(x, y))?.data == true) {
                2.pow(y * grid.width + x)
            } else {
                0
            }
        }
    }

data class TileWithDepth(val depth: Int, val tile: Tile<Boolean>)

private class RecursiveGrids(initialGrid: Grid<Boolean>) {
    private val grids = mutableMapOf(0 to initialGrid)

    fun allTiles() =
        grids.entries
            .flatMap { (depth, grid) -> grid.tiles.map { TileWithDepth(depth, it) } }
            .filter { it.tile.point != Point2D(2, 2) }

    fun adjacent(tileWithDepth: TileWithDepth): List<TileWithDepth> {
        val (depth, tile) = tileWithDepth

        val thisGridNeighbours = tile.adjacentOrthogonally().filter { it.point != Point2D(2, 2) }
        val higherGrid = gridAtDepth(depth + 1)
        val lowerGrid = gridAtDepth(depth - 1)

        val centerTile = tile.grid.tileAt(Point2D(2, 2)) ?: error("Grid has no center")
        val lowerGridNeighbours =
            when (centerTile) {
                tile.north -> lowerGrid.southEdge()
                tile.east -> lowerGrid.westEdge()
                tile.south -> lowerGrid.northEdge()
                tile.west -> lowerGrid.eastEdge()
                else -> emptyList()
            }

        val higherGridNeighbours = buildList {
            val higherGridCenter =
                higherGrid.tileAt(Point2D(2, 2)) ?: error("Higher grid has no center")
            if (tile in tile.grid.northEdge()) {
                add(higherGridCenter.north!!)
            }

            if (tile in tile.grid.eastEdge()) {
                add(higherGridCenter.east!!)
            }

            if (tile in tile.grid.westEdge()) {
                add(higherGridCenter.west!!)
            }

            if (tile in tile.grid.southEdge()) {
                add(higherGridCenter.south!!)
            }
        }

        return thisGridNeighbours.map { TileWithDepth(depth, it) } +
            higherGridNeighbours.map { TileWithDepth(depth + 1, it) } +
            lowerGridNeighbours.map { TileWithDepth(depth - 1, it) }
    }

    fun gridAtDepth(depth: Int) = grids.computeIfAbsent(depth) { emptyGrid() }

    fun updateDepth(depth: Int, block: (Grid<Boolean>) -> Grid<Boolean>) {
        grids[depth] = block(gridAtDepth(depth))
    }

    private fun emptyGrid(): Grid<Boolean> {
        val tiles = (0 until 5).flatMap { x -> (0 until 5).map { y -> Tile(Point2D(x, y), false) } }

        return Grid(tiles)
    }
}

fun main() {
    val input = readResourceAsString("/day24.txt")

    fun parseGrid(input: String): Grid<Boolean> {
        val grid =
            parseGrid(input) {
                when (it) {
                    '.' -> false
                    '#' -> true
                    else -> error("Unknown char: $it")
                }
            }
        return grid
    }

    part1 {
        val grid = parseGrid(input)
        val grids = mutableListOf(grid)
        while (true) {
            val new = stepPart1(grids.last())
            val duplicate = grids.find { it.hasSameTiles(new) }
            if (duplicate != null) {
                return@part1 biodiversityRating(duplicate)
            }
            grids += new
        }
    }

    part2 {
        val recursiveGrids = RecursiveGrids(parseGrid(input))

        repeat(200) { stepPart2(recursiveGrids) }

        recursiveGrids.allTiles().count { it.tile.data }
    }
}
