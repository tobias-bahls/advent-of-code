import Day18GridTile.Door
import Day18GridTile.Floor
import Day18GridTile.Key
import Day18GridTile.Wall
import algorithms.dijkstra
import algorithms.dijkstraPath
import datastructures.CardinalDirection.*
import datastructures.Grid
import datastructures.Point2D
import datastructures.Tile
import datastructures.movementVector
import datastructures.parseAsciiGrid
import datastructures.parseGrid
import utils.Scored
import utils.cartesian
import utils.part1
import utils.part2
import utils.readResourceAsString

private sealed interface Day18GridTile {
    fun canWalk(keys: Set<Char>): Boolean

    object Wall : Day18GridTile {
        override fun canWalk(keys: Set<Char>) = false
    }
    object Floor : Day18GridTile {
        override fun canWalk(keys: Set<Char>) = true
    }
    data class Key(val color: Char) : Day18GridTile {
        override fun canWalk(keys: Set<Char>) = true
    }
    data class Door(val color: Char) : Day18GridTile {
        override fun canWalk(keys: Set<Char>) = color in keys
    }
}

private class PathCache(paths: List<PrecomputedPath>) {
    val cache = paths.associateBy { it.from to it.to }

    data class PrecomputedPath(
        val from: Tile<Day18GridTile>,
        val to: Tile<Day18GridTile>,
        val path: List<Tile<Day18GridTile>>
    ) {
        val requiredKeys = path.filter { it.data is Door }.map { (it.data as Door).color }
        val steps = path.size - 1
    }

    fun getPath(
        from: Tile<Day18GridTile>,
        to: Tile<Day18GridTile>,
        keys: Set<Char>
    ): PrecomputedPath? {
        val path = cache[from to to] ?: return null

        return if (!keys.containsAll(path.requiredKeys)) {
            null
        } else {
            path
        }
    }

    fun getAllPaths(from: Tile<Day18GridTile>, keys: Set<Char>): List<PrecomputedPath> {
        return cache.keys.filter { it.first == from }.mapNotNull { getPath(from, it.second, keys) }
    }
}

private fun createPathCache(
    grid: Grid<Day18GridTile>,
    robotTiles: List<Tile<Day18GridTile>>
): PathCache {
    val keyTiles = grid.tiles.filter { it.data is Key }

    val paths =
        (keyTiles + robotTiles)
            .let { it.cartesian(it) }
            .filter { (startTile, endTile) -> startTile != endTile }
            .filter { (_, endTile) -> endTile !in robotTiles }
            .map { (startTile, endTile) ->
                dijkstraPath<Tile<Day18GridTile>> {
                        start = startTile
                        end = endTile

                        neighbours { current ->
                            current
                                .adjacentOrthogonally()
                                .filter { it.data !is Wall }
                                .map { Scored(1, it) }
                        }
                    }
                    ?.let { PathCache.PrecomputedPath(startTile, endTile, it) }
            }
            .filterNotNull()
            .toList()

    return PathCache(paths)
}

private data class Day18StateStaticData(
    val pathCache: PathCache,
    val grid: Grid<Day18GridTile>,
)

private data class Day18Robot(
    val position: Tile<Day18GridTile>,
    val keys: Set<Char> = emptySet(),
) {
    fun getNeighboursPaths(
        staticData: Day18StateStaticData,
        allKeys: Set<Char>
    ): List<PathCache.PrecomputedPath> {
        return staticData.pathCache.getAllPaths(position, allKeys).filter {
            (it.to.data as Key).color !in allKeys
        }
    }
    fun walkPath(path: PathCache.PrecomputedPath): Day18Robot {
        val newKeys = keys + (path.to.data as Key).color

        return copy(keys = newKeys, position = path.to)
    }
}

private data class Day18State(
    val staticData: Day18StateStaticData,
    val robots: List<Day18Robot>,
    val steps: Int = 0
) {
    val keys = robots.flatMap { it.keys }.distinct().toSet()

    fun getNextMoves(): List<Pair<Day18Robot, PathCache.PrecomputedPath>> {
        return robots.flatMap { robot ->
            robot.getNeighboursPaths(staticData, keys).map { path -> robot to path }
        }
    }

    fun walkPath(robot: Day18Robot, path: PathCache.PrecomputedPath): Day18State {
        val newRobots =
            robots.map {
                if (it == robot) {
                    robot.walkPath(path)
                } else {
                    it
                }
            }

        return copy(robots = newRobots, steps = steps + path.steps)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Day18State

        if (robots != other.robots) return false

        return true
    }

    override fun hashCode(): Int {
        return robots.hashCode()
    }
}

private fun findRobotPosition(input: String) =
    parseAsciiGrid(input) { x, y, char ->
            if (char != '@') return@parseAsciiGrid null

            Point2D(x, y)
        }
        .single()

private fun parseMap(input: String) =
    parseGrid(input) { char ->
        when (char) {
            '#' -> Wall
            '@' -> Floor
            '.' -> Floor
            else ->
                when {
                    char.isLowerCase() -> Key(char.uppercaseChar())
                    char.isUpperCase() -> Door(char.uppercaseChar())
                    else -> error("Unknown char: $char")
                }
        }
    }

private fun createState(
    grid: Grid<Day18GridTile>,
    robotTiles: List<Tile<Day18GridTile>>
): Day18State {
    val pathCache = createPathCache(grid, robotTiles)
    val staticData = Day18StateStaticData(pathCache, grid)

    return Day18State(staticData, robotTiles.map { Day18Robot(it) })
}

fun main() {
    val input = readResourceAsString("/day18.txt")
    val parsed = parseMap(input)
    val robotPosition = findRobotPosition(input)
    val robotTile = parsed.tileAt(robotPosition) ?: error("Could not find player tile")

    part1 { solve(createState(parsed, listOf(robotTile))) }

    part2 {
        val toBeConvertedIntoWalls =
            robotTile.adjacentOrthogonally().map { it.point } + robotTile.point
        val robots =
            listOf(NorthEast, SouthEast, SouthWest, NorthWest).map {
                robotTile.point + it.movementVector
            }

        val tiles: List<Tile<Day18GridTile>> =
            parsed.tiles.map {
                if (it.point in toBeConvertedIntoWalls) {
                    Tile(it.point, Wall)
                } else {
                    it
                }
            }

        val adjustedGrid = Grid(tiles)
        val robotTiles = robots.map { adjustedGrid.tileAt(it)!! }

        solve(createState(parsed, robotTiles))
    }
}

private fun solve(state: Day18State): Int {
    val allKeys =
        state.staticData.grid.tiles.filter { it.data is Key }.map { (it.data as Key).color }.toSet()

    return dijkstra<Day18State> {
            start = state

            endCondition { it.keys == allKeys }

            neighbours { current ->
                current.getNextMoves().map { (robot, path) ->
                    val newState = current.walkPath(robot, path)
                    Scored(path.steps, newState)
                }
            }
        }!!
        .steps
}
