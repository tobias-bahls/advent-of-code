import datastructures.Point3D
import datastructures.parsePoint3D
import io.kotest.matchers.collections.shouldContainAll
import utils.Test
import utils.cartesian
import utils.filterNotBlank
import utils.match
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.runTests
import utils.singleMatch

private val allRotations =
    listOf(
        arrayOf(intArrayOf(1, 0, 0), intArrayOf(0, 1, 0), intArrayOf(0, 0, 1)),
        arrayOf(intArrayOf(1, 0, 0), intArrayOf(0, 0, -1), intArrayOf(0, 1, 0)),
        arrayOf(intArrayOf(1, 0, 0), intArrayOf(0, -1, 0), intArrayOf(0, 0, -1)),
        arrayOf(intArrayOf(1, 0, 0), intArrayOf(0, 0, 1), intArrayOf(0, -1, 0)),
        //
        arrayOf(intArrayOf(0, -1, 0), intArrayOf(1, 0, 0), intArrayOf(0, 0, 1)),
        arrayOf(intArrayOf(0, 0, 1), intArrayOf(1, 0, 0), intArrayOf(0, 1, 0)),
        arrayOf(intArrayOf(0, 1, 0), intArrayOf(1, 0, 0), intArrayOf(0, 0, -1)),
        arrayOf(intArrayOf(0, 0, -1), intArrayOf(1, 0, 0), intArrayOf(0, -1, 0)),
        //
        arrayOf(intArrayOf(-1, 0, 0), intArrayOf(0, -1, 0), intArrayOf(0, 0, 1)),
        arrayOf(intArrayOf(-1, 0, 0), intArrayOf(0, 0, -1), intArrayOf(0, -1, 0)),
        arrayOf(intArrayOf(-1, 0, 0), intArrayOf(0, 1, 0), intArrayOf(0, 0, -1)),
        arrayOf(intArrayOf(-1, 0, 0), intArrayOf(0, 0, 1), intArrayOf(0, 1, 0)),
        //
        arrayOf(intArrayOf(0, 1, 0), intArrayOf(-1, 0, 0), intArrayOf(0, 0, 1)),
        arrayOf(intArrayOf(0, 0, 1), intArrayOf(-1, 0, 0), intArrayOf(0, -1, 0)),
        arrayOf(intArrayOf(0, -1, 0), intArrayOf(-1, 0, 0), intArrayOf(0, 0, -1)),
        arrayOf(intArrayOf(0, 0, -1), intArrayOf(-1, 0, 0), intArrayOf(0, 1, 0)),
        //
        arrayOf(intArrayOf(0, 0, -1), intArrayOf(0, 1, 0), intArrayOf(1, 0, 0)),
        arrayOf(intArrayOf(0, 1, 0), intArrayOf(0, 0, 1), intArrayOf(1, 0, 0)),
        arrayOf(intArrayOf(0, 0, 1), intArrayOf(0, -1, 0), intArrayOf(1, 0, 0)),
        arrayOf(intArrayOf(0, -1, 0), intArrayOf(0, 0, -1), intArrayOf(1, 0, 0)),
        //
        arrayOf(intArrayOf(0, 0, -1), intArrayOf(0, -1, 0), intArrayOf(-1, 0, 0)),
        arrayOf(intArrayOf(0, -1, 0), intArrayOf(0, 0, 1), intArrayOf(-1, 0, 0)),
        arrayOf(intArrayOf(0, 0, 1), intArrayOf(0, 1, 0), intArrayOf(-1, 0, 0)),
        arrayOf(intArrayOf(0, 1, 0), intArrayOf(0, 0, -1), intArrayOf(-1, 0, 0)),
    )

private fun allRotations(point: Point3D) = allRotations.map { point.transform(it) }

private data class Scanner(val index: Int, val beacons: Set<Point3D>) {
    fun rotate(by: Array<IntArray>): Scanner =
        Scanner(index, beacons.map { it.transform(by) }.toSet())
}

private fun parseScanner(input: String): Scanner {
    val lines = input.lines().filterNotBlank()
    val index = lines.first().match("""scanner (\d+)""").singleMatch().let { it.toInt() }

    return Scanner(index, lines.drop(1).map { parsePoint3D(it) }.toSet())
}

fun main() {
    runTests()

    val input = readResourceAsString("/day19.txt")
    val scanners = input.split("\n\n").map { parseScanner(it) }

    val results = mutableListOf<Result>()
    part1 {
        val first = scanners[0]
        val composite = mutableSetOf<Point3D>()
        composite.addAll(first.beacons)
        val remainingScanners = scanners.drop(1).toMutableSet()

        while (remainingScanners.isNotEmpty()) {
            val result = findOverlappingScanner(remainingScanners, composite)
            composite += result.scanner.beacons.map { it + result.offset }
            remainingScanners.removeIf { it.index == result.scanner.index }

            results += result
        }

        composite.size
    }

    part2 {
        val offsets = results.map { it.offset } + Point3D(0, 0, 0)
        offsets.cartesian(offsets).maxOf { (a, b) -> a.manhattanDistanceTo(b) }
    }
}

private data class Result(val scanner: Scanner, val offset: Point3D)

private fun findOverlappingScanner(
    remainingScanners: Set<Scanner>,
    composite: Set<Point3D>,
): Result {
    remainingScanners.cartesian(allRotations).forEach { (scanner, rotation) ->
        val rotated = scanner.rotate(rotation)

        rotated.beacons.cartesian(composite).forEach { (beacon, other) ->
            val offset = other - beacon

            val foundBeacons =
                rotated.beacons.map { it + offset }.filter { it in composite }.toSet()

            if (foundBeacons.size >= 12) {
                return Result(rotated, offset)
            }
        }
    }

    error("Unreachable")
}

@Test
private fun allRotationsTest() {
    allRotations(Point3D(-1, -1, 1)) shouldContainAll
        listOf(
            Point3D(-1, -1, 1),
            Point3D(1, -1, 1),
            Point3D(-1, -1, -1),
            Point3D(1, 1, -1),
            Point3D(1, 1, 1))
}
