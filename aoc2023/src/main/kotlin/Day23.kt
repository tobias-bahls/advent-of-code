import algorithms.repeat
import datastructures.*
import datastructures.CardinalDirection.*
import utils.*

private sealed interface Day23Tile

private data object Floor : Day23Tile

private data object Forest : Day23Tile

private data class Slope(val dir: CardinalDirectionOrthogonal) : Day23Tile

private fun parseGrid(input: String): Grid<Day23Tile> {
    val grid =
        parseGrid(input) {
            when (it) {
                '#' -> Forest
                '.' -> Floor
                '>' -> Slope(East)
                'v' -> Slope(South)
                '<' -> Slope(West)
                '^' -> Slope(North)
                else -> unreachable("tile $it")
            }
        }
    return grid
}

fun main() {
    part1 {
        val input = readResourceAsString("/day23.txt")
        val grid = parseGrid(input)

        val startTile = grid.row(0).single { it.data is Floor }
        val endTile = grid.row(grid.height - 1).single { it.data is Floor }

        fun longestPath(
            from: Tile<Day23Tile>,
            to: Tile<Day23Tile>,
            seen: Set<Tile<Day23Tile>>
        ): Int {
            val data = from.data
            val neighbours =
                if (data is Floor) {
                    from.adjacentOrthogonally()
                } else {
                    check(data is Slope)
                    listOf(from.directNeighbourInDirection(data.dir)!!)
                }

            val validNeighbours = neighbours.filter { it.data !is Forest && it !in seen }
            val result =
                when {
                    validNeighbours.isEmpty() -> 0
                    to in validNeighbours -> 1
                    else -> validNeighbours.maxOf { longestPath(it, to, seen + it) + 1 }
                }

            return result
        }

        longestPath(startTile, endTile, setOf(startTile))
    }

    part2 {
        val input = readResourceAsString("/day23.txt")
        val grid = parseGrid(input)

        val startTile = grid.row(0).single { it.data is Floor }
        val endTile = grid.row(grid.height - 1).single { it.data is Floor }

        data class Edge(val from: Tile<Day23Tile>, val to: Tile<Day23Tile>, val length: Int)
        fun findJunctions(from: Tile<Day23Tile>): List<Edge> {
            val fromNeighbours = from.adjacentOrthogonally().filter { it.data !is Forest }

            return fromNeighbours.map { neighbour ->
                val seen = mutableSetOf(from)
                val result =
                    repeat(neighbour) { current ->
                        seen.add(current)
                        val neighbours =
                            current.adjacentOrthogonally().filter {
                                it.data !is Forest && it !in seen
                            }
                        if (neighbours.size == 1) {
                            next(neighbours.single())
                        } else {
                            stop()
                        }
                    }

                Edge(from, result.element, result.iterations + 1)
            }
        }

        val seen = mutableSetOf<Tile<Day23Tile>>()
        val edges = mutableListOf<Edge>()
        queue(startTile) { tile ->
            if (tile in seen) {
                return@queue skip()
            }
            seen.add(tile)

            val junctions = findJunctions(tile)
            edges.addAll(junctions)
            enqueue(edges.map { it.to })
        }

        data class State(val path: List<Tile<Day23Tile>>, val score: Int) {
            private val pathSet = path.toSet()

            operator fun contains(tile: Tile<Day23Tile>) = tile in pathSet
        }

        val paths = mutableListOf<State>()
        queue(State(listOf(startTile), 0)) { currentPath ->
            val tile = currentPath.path.last()
            if (tile == endTile) {
                paths.add(currentPath)
                return@queue skip()
            }

            val neighbours = edges.filter { it.from == tile }
            val validNeighbours = neighbours.filter { it.to !in currentPath }

            if (validNeighbours.isEmpty()) {
                skip()
            } else {
                validNeighbours
                    .map { State(currentPath.path + it.to, currentPath.score + it.length) }
                    .let { enqueue(it) }
            }
        }

        paths.maxOf { it.score }
    }
}
