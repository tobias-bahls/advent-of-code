import datastructures.Line
import datastructures.Point2D
import datastructures.SparseGrid
import datastructures.parsePoint2D
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString

private fun pointsOnLine(line: Line): List<Point2D> {
    return when {
        line.from.x == line.to.x -> {
            val low = line.from.y.coerceAtMost(line.to.y)
            val high = line.from.y.coerceAtLeast(line.to.y)
            (low..high).map { Point2D(line.from.x, it) }
        }
        line.from.y == line.from.y -> {
            val low = line.from.x.coerceAtMost(line.to.x)
            val high = line.from.x.coerceAtLeast(line.to.x)
            (low..high).map { Point2D(it, line.from.y) }
        }
        else -> error("Can not find points for line $line")
    }
}

private fun parsePaths(input: String) =
    input.split(" -> ").map { parsePoint2D(it) }.windowed(2).map { (a, b) -> Line(a, b) }

private fun determineSandPosition(tileMap: SparseGrid): Point2D {
    var sandPosition = Point2D(500, 0)

    while (sandPosition.y <= tileMap.height) {
        if (!tileMap.hasPoint(sandPosition.bottom)) {
            sandPosition = sandPosition.bottom
            continue
        }
        if (!tileMap.hasPoint(sandPosition.bottomLeft)) {
            sandPosition = sandPosition.bottomLeft
            continue
        }
        if (!tileMap.hasPoint(sandPosition.bottomRight)) {
            sandPosition = sandPosition.bottomRight
            continue
        }

        break
    }

    return sandPosition
}

private fun determineSandPositionPart2(tileMap: SparseGrid): Point2D {
    var sandPosition = Point2D(500, 0)
    fun hasPointOrIsFloor(point: Point2D): Boolean =
        tileMap.hasPoint(point) || point.y == tileMap.height + 2

    while (sandPosition.y <= tileMap.height + 2) {
        if (!hasPointOrIsFloor(sandPosition.bottom)) {
            sandPosition = sandPosition.bottom
            continue
        }
        if (!hasPointOrIsFloor(sandPosition.bottomLeft)) {
            sandPosition = sandPosition.bottomLeft
            continue
        }
        if (!hasPointOrIsFloor(sandPosition.bottomRight)) {
            sandPosition = sandPosition.bottomRight
            continue
        }

        break
    }

    return sandPosition
}

private fun solve(tileMap: SparseGrid): Int {
    var loopCount = 0
    while (true) {
        val sandPosition = determineSandPosition(tileMap)
        if (sandPosition.y > tileMap.height) {
            return loopCount
        }
        loopCount++
        tileMap.addPoint(sandPosition)
    }
}

private fun solvePart2(tileMap: SparseGrid): Int {
    var loopCount = 0
    while (true) {
        val sandPosition = determineSandPositionPart2(tileMap)
        if (sandPosition == Point2D(500, 0)) {
            return loopCount + 1
        }
        loopCount++
        tileMap.addPoint(sandPosition)
    }
}

fun main() {
    val input = readResourceAsString("/day14.txt")

    val allBoulders =
        input.parseLines { parsePaths(it) }.flatten().flatMap { pointsOnLine(it) }.toSet()

    part1 {
        val tileMap = SparseGrid(allBoulders)
        solve(tileMap)
    }

    part2 {
        val tileMap = SparseGrid(allBoulders)

        solvePart2(tileMap)
    }
}
