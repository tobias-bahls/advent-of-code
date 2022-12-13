import datastructures.Point2D
import kotlin.math.absoluteValue
import utils.match
import utils.part1
import utils.part2

private data class TargetArea(val x: IntRange, val y: IntRange)

private fun step(point: Point2D, velocity: Point2D): Pair<Point2D, Point2D> {
    val newPoint = point + velocity
    val newX =
        when {
            velocity.x == 0 -> 0
            velocity.x > 0 -> velocity.x - 1
            else -> velocity.x + 1
        }

    val newVelocity = Point2D(x = newX, y = velocity.y - 1)

    return Pair(newPoint, newVelocity)
}

private fun hitsTargetArea(
    initialPoint: Point2D,
    initialVelocity: Point2D,
    targetArea: TargetArea
): Boolean {
    var velocity = initialVelocity
    var currentPoint = initialPoint

    while (currentPoint.x <= targetArea.x.last && currentPoint.y >= targetArea.y.first) {
        val (newPoint, newVelocity) = step(currentPoint, velocity)

        velocity = newVelocity
        currentPoint = newPoint

        if (currentPoint.x in targetArea.x && currentPoint.y in targetArea.y) {
            return true
        }
    }

    return false
}

fun main() {
    val targetArea =
        "target area: x=241..273, y=-97..-63"
            .match("""target area: x=(-?\d+)..(-?\d+), y=(-?\d+)..(-?\d+)""")
            .toList()
            .map { it.toInt() }
            .let { (xMin, xMax, yMin, yMax) -> TargetArea(xMin..xMax, yMin..yMax) }

    part1 { 0.until(targetArea.y.first * -1).sum() }

    part2 {
        val maxYAbs = targetArea.y.first.absoluteValue + 1
        val maxXAbs = targetArea.x.last.absoluteValue + 1

        (-maxYAbs..maxXAbs)
            .flatMap { yVelocity ->
                (-maxXAbs..maxXAbs).map { xVelocity -> Point2D(xVelocity, yVelocity) }
            }
            .filter { hitsTargetArea(Point2D(0, 0), it, targetArea) }
            .size
    }
}
