import datastructures.Point3D
import datastructures.parsePoint3D
import utils.extend
import utils.floodFill
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toRangeBy

fun main() {
    val input = readResourceAsString("/day18.txt")
    val points = input.parseLines { parsePoint3D(it) }.toSet()

    part1 {
        points.sumOf { point ->
            val coveredSides =
                point.neighboursOrthogonally.filter { neighbour -> neighbour in points }
            6 - coveredSides.size
        }
    }

    part2 {
        val xRange = points.toRangeBy { it.x }.extend(1)
        val yRange = points.toRangeBy { it.y }.extend(1)
        val zRange = points.toRangeBy { it.z }.extend(1)

        val outsidePoints =
            floodFill(
                initial = Point3D(xRange.first, yRange.first, zRange.first),
                determineNeighbours = { it.neighboursOrthogonally },
                extraStopConditions =
                    listOf(
                        { it in points },
                        { it in points || it.x !in xRange || it.y !in yRange || it.z !in zRange }))

        points.sumOf { point ->
            point.neighboursOrthogonally.filter { neighbour -> neighbour in outsidePoints }.size
        }
    }
}
