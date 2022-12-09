import datastructures.Graph
import datastructures.Grid
import datastructures.Point
import datastructures.Tile
import datastructures.dijkstra
import utils.part1
import utils.part2
import utils.readResourceAsString

fun createGraphFromGrid(grid: Grid<Int>): Graph<Point, Int, Int> {
    val graph = Graph<Point, Int, Int>()

    grid.tiles.forEach { graph.addNode(it.point, it.data) }

    grid.tiles.forEach { it ->
        val thisNode = graph.getNode(it.point)
        it.point.neighboursOrthogonally.forEach { n ->
            val neighbourRisk = grid.tileAt(n)?.data

            if (neighbourRisk != null) {
                val neighbourNode = graph.getNode(n)
                graph.addEdge(thisNode, neighbourNode, neighbourRisk)
            }
        }
    }

    return graph
}

fun enlargeGrid(grid: Grid<Int>) {
    fun calculateNewRisk(tile: Tile<Int>, n: Int): Int =
        (tile.data + n).let {
            if (it < 10) {
                it
            } else {
                it - 9
            }
        }

    val originalWidth = grid.width
    grid.tiles.toList().forEach { tile ->
        (1..4).map { n ->
            val newRisk = calculateNewRisk(tile, n)

            val p1 = Point(tile.point.x + originalWidth * n, tile.point.y)
            grid.addTile(p1, newRisk)
        }
    }

    val originalHeight = grid.height
    grid.tiles.toList().forEach { tile ->
        (1..4).map { n ->
            val newRisk = calculateNewRisk(tile, n)

            val p1 = Point(tile.point.x, tile.point.y + originalHeight * n)
            grid.addTile(p1, newRisk)
        }
    }
}

fun solve(graph: Graph<Point, Int, Int>, grid: Grid<Int>): Int {
    val start = graph.getNode(Point(0, 0))
    val end = graph.getNode(Point(grid.width - 1, grid.height - 1))

    return dijkstra(start, end).drop(1).sumOf { it.data }
}

fun main() {
    val input = readResourceAsString("/day15.txt")

    part1 {
        val grid = Grid(input) { it.digitToInt() }
        val graph = createGraphFromGrid(grid)

        solve(graph, grid)
    }

    part2 {
        val grid = Grid(input) { it.digitToInt() }
        enlargeGrid(grid)
        val graph = createGraphFromGrid(grid)

        solve(graph, grid)
    }
}
