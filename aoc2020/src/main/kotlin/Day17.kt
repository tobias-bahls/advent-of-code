import datastructures.Point3D
import datastructures.parseAsciiGrid
import utils.cartesian
import utils.extend
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.reduceTimes
import utils.toRangeBy
import utils.toTriple

private data class Point4D(val x: Int, val y: Int, val z: Int, val w: Int) {
    companion object {
        val ZERO = Point4D(0, 0, 0, 0)

        val allNeighbourVectors =
            listOf(-1, 0, 1)
                .let { it.cartesian(it).cartesian(it).cartesian(it) }
                .map { it.toTriple() }
                .map { (xy, z, w) ->
                    val (x, y) = xy
                    Point4D(x, y, z, w)
                }
                .filter { it != ZERO }
                .toList()
    }

    val neighbours
        get() = allNeighbourVectors.map { this + it }

    operator fun plus(other: Point4D) =
        Point4D(
            x = x + other.x,
            y = y + other.y,
            z = z + other.z,
            w = w + other.w,
        )
}

fun main() {
    val input = readResourceAsString("/day17.txt")
    val initialGrid =
        parseAsciiGrid(input) { x, y, char ->
                if (char == '#') {
                    Point3D(x, y, 0)
                } else {
                    null
                }
            }
            .toSet()

    part1 {
        reduceTimes(6, initialGrid) { grid ->
                val xRange = grid.toRangeBy { it.x }.extend(3)
                val yRange = grid.toRangeBy { it.y }.extend(3)
                val zRange = grid.toRangeBy { it.z }.extend(3)

                xRange
                    .cartesian(yRange)
                    .cartesian(zRange)
                    .map { it.toTriple() }
                    .map { (x, y, z) -> Point3D(x, y, z) }
                    .mapNotNull { point ->
                        val activeNeighbours = point.neighbours.count { it in grid }

                        when {
                            point in grid && (activeNeighbours == 2 || activeNeighbours == 3) ->
                                point
                            point !in grid && activeNeighbours == 3 -> point
                            else -> null
                        }
                    }
                    .toSet()
            }
            .size
    }
    part2 {
        val initial4DGrid = initialGrid.map { Point4D(it.x, it.y, it.z, 0) }.toSet()

        reduceTimes(6, initial4DGrid) { grid ->
                val xRange = grid.toRangeBy { it.x }.extend(3)
                val yRange = grid.toRangeBy { it.y }.extend(3)
                val zRange = grid.toRangeBy { it.z }.extend(3)
                val wRange = grid.toRangeBy { it.w }.extend(3)

                xRange
                    .cartesian(yRange)
                    .cartesian(zRange)
                    .cartesian(wRange)
                    .map { (xyz, w) ->
                        val (xy, z) = xyz
                        val (x, y) = xy
                        Point4D(x, y, z, w)
                    }
                    .mapNotNull { point ->
                        val activeNeighbours = point.neighbours.count { it in grid }

                        when {
                            point in grid && (activeNeighbours == 2 || activeNeighbours == 3) ->
                                point
                            point !in grid && activeNeighbours == 3 -> point
                            else -> null
                        }
                    }
                    .toSet()
            }
            .size
    }
}
