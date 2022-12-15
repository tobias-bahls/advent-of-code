import datastructures.Point2D
import java.util.BitSet
import kotlin.math.absoluteValue
import utils.match
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toIntSet

private class ScanLine(val size: Int) {
    var scannedPoints = BitSet(size + 1)

    val completelyScanned
        get() = scannedPoints.cardinality() == size + 1

    val missingPoint
        get() = ((0..size).toSet() - scannedPoints.toIntSet()).first()

    fun addScanned(range: IntRange) {
        val from = range.first.coerceAtLeast(0)
        val to = range.last.coerceAtMost(size)
        scannedPoints.set(from, to + 1)
    }

    fun reset() {
        scannedPoints.clear()
    }
}

private data class Sensor(val position: Point2D, val closestBeacon: Point2D) {

    fun updateScanLine(scanLine: ScanLine, row: Int) {
        val distance = position.manhattanDistanceTo(closestBeacon)

        val yOffs = row - this.position.y

        if (position.y + yOffs != row || yOffs.absoluteValue > distance) {
            return
        }

        val xRange = (yOffs.absoluteValue - distance).absoluteValue
        val coveredColumns = (position.x - xRange..position.x + xRange)
        scanLine.addScanned(coveredColumns)
    }

    fun coveredPointsAtRow(row: Int): List<Point2D> {
        val distance = position.manhattanDistanceTo(closestBeacon)

        val yOffs = row - this.position.y

        if (position.y + yOffs != row) {
            return emptyList()
        }

        val xRange = (yOffs.absoluteValue - distance).absoluteValue
        return (-xRange..xRange).map { xOffs -> Point2D(position.x + xOffs, position.y + yOffs) }
    }
}

private fun parseSensor(input: String): Sensor {
    val (x1, y1, x2, y2) =
        input
            .match("""Sensor at x=(-?\d+), y=(-?\d+): closest beacon is at x=(-?\d+), y=(-?\d+)""")
            .toList()
            .map { it.toInt() }
    return Sensor(Point2D(x1, y1), Point2D(x2, y2))
}

fun main() {
    val input = readResourceAsString("/day15.txt")
    val sensors = input.parseLines { parseSensor(it) }

    part1 {
        val allBeacons = sensors.map { it.closestBeacon }.filter { it.y == 2000000 }.toSet()
        val allPoints = sensors.flatMap { it.coveredPointsAtRow(2000000) }.toSet()

        allPoints.size - allBeacons.size
    }

    part2 {
        val area = 4000000

        val line = ScanLine(area)
        (0 until area).forEach { row ->
            sensors.forEach { it.updateScanLine(line, row) }
            if (!line.completelyScanned) {
                return@part2 (line.missingPoint * 4000000L) + row
            }
            line.reset()
        }

        error("Did not find result")
    }
}
