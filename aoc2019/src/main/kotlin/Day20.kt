import algorithms.dijkstraPath
import datastructures.Grid
import datastructures.Point2D
import datastructures.Tile
import datastructures.parseAsciiGrid
import utils.*

sealed interface Day20Tile {
    data object Empty : Day20Tile

    data object Floor : Day20Tile

    data object Wall : Day20Tile

    data class PortalPart(val name: String) : Day20Tile
}

data class Portal(val parts: Set<Tile<Day20Tile>>, val exit: Tile<Day20Tile>) {
    val identifier = parts.map { (it.data as Day20Tile.PortalPart).name }.sorted().joinToString("")
    val start = identifier == "AA"
    val end = identifier == "ZZ"
    private val grid = exit.grid

    val outer =
        parts
            .flatMap { listOf(it.point.x, it.point.y) }
            .any { it in listOf(0, grid.width - 1, grid.height - 1) }

    infix fun connectsTo(other: Portal) = this != other && this.identifier == other.identifier
}

data class Day20Maze(val grid: Grid<Day20Tile>) {
    val portals =
        grid.tiles
            .mapNotNull { tile ->
                if (tile.data !is Day20Tile.PortalPart) {
                    return@mapNotNull null
                }

                val otherPart =
                    tile.adjacentOrthogonally().find { it.data is Day20Tile.PortalPart }
                        ?: error("No other portal part found for $tile")
                val exit =
                    (tile.adjacentOrthogonally() + otherPart.adjacentOrthogonally()).find {
                        it.data is Day20Tile.Floor
                    }
                        ?: error("No exit found for $tile")

                Portal(setOf(tile, otherPart), exit)
            }
            .distinct()

    val connections =
        portals
            .filter { !it.start && !it.end }
            .associateBy { portal ->
                portals.find { other -> portal connectsTo other }
                    ?: error("Could not find connection for portal $portal")
            }
    val start = portals.find { it.start }.dump("Start") ?: error("Could not find start point")
    val end = portals.find { it.end }.dump("End") ?: error("Could not find end point")

    fun portalEntryExitAtTile(entry: Tile<Day20Tile>): Portal =
        portals.find { it.exit == entry } ?: error("Could not find portal at $entry")

    fun portalOtherEnd(entry: Tile<Day20Tile>): Portal =
        connections.entries.find { (key, value) -> key.exit == entry }?.value
            ?: error("Could not find portal connection for entry point $entry")
}

fun main() {
    val input = readResourceAsString("/day20.txt")

    val grid =
        Grid(
            parseAsciiGrid(input.lines(), trim = false) { x, y, char ->
                val tile =
                    when (char) {
                        ' ' -> Day20Tile.Empty
                        '#' -> Day20Tile.Wall
                        '.' -> Day20Tile.Floor
                        else -> Day20Tile.PortalPart(char.toString())
                    }
                Tile(Point2D(x, y), tile)
            })

    val maze = Day20Maze(grid)

    part1 { part1(maze) }
    part2 { part2(maze) }
}

private fun part1(maze: Day20Maze): Int {
    val result =
        maze.grid.dijkstra(maze.start.exit, maze.end.exit) { tile ->
            tile
                .adjacentOrthogonally()
                .mapNotNull {
                    when {
                        it.data is Day20Tile.Floor -> it
                        it in maze.end.parts -> it
                        it in maze.start.parts -> null
                        it.data is Day20Tile.PortalPart -> maze.portalOtherEnd(tile).exit
                        else -> null
                    }
                }
                .map { Scored(1, it) }
        }

    return result!!.size - 1
}

private fun part2(maze: Day20Maze): Int {
    data class DepthTile(val depth: Int, val tile: Tile<Day20Tile>)

    val result =
        dijkstraPath<DepthTile> {
            start = DepthTile(0, maze.start.exit)
            end = DepthTile(0, maze.end.exit)

            neighbours { (depth, tile) ->
                tile
                    .adjacentOrthogonally()
                    .mapNotNull {
                        when {
                            it in maze.start.parts -> null
                            it in maze.end.parts && depth != 0 -> null
                            it.data is Day20Tile.Floor -> DepthTile(depth, it)
                            it in maze.end.parts && depth == 0 -> DepthTile(0, it)
                            it.data is Day20Tile.PortalPart -> {
                                val thisEnd = maze.portalEntryExitAtTile(tile)
                                if (thisEnd.outer && depth == 0) {
                                    return@mapNotNull null
                                }
                                val otherEnd = maze.portalOtherEnd(tile)
                                val newDepth =
                                    if (thisEnd.outer) {
                                        depth - 1
                                    } else {
                                        depth + 1
                                    }

                                DepthTile(newDepth, otherEnd.exit)
                            }
                            else -> null
                        }
                    }
                    .map { Scored(1 + it.depth, it) }
            }
        }

    return result!!.size - 1
}
