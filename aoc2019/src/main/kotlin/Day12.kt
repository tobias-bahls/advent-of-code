import datastructures.Point3D
import kotlin.reflect.KProperty1
import utils.cartesian
import utils.filterNotBlank
import utils.lcm
import utils.mapValues
import utils.match
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.reduceTimes
import utils.repeatUntilTrue

private data class Moon(val id: Int, val position: Point3D, val velocity: Point3D) {
    private val potentialEnergy = position.absoluteValue.components.sum()
    private val kineticEnergy = velocity.absoluteValue.components.sum()

    val energy = potentialEnergy * kineticEnergy

    fun applyVelocity(velocityChange: Point3D): Moon {
        val newVelocity = velocity + velocityChange
        val newPosition = position + newVelocity

        return copy(position = newPosition, velocity = newVelocity)
    }
}

private fun parseMoon(id: Int, line: String): Moon {
    val (x, y, z) =
        line.match("""<x=(-?[0-9]+), y=(-?[0-9]+), z=(-?[0-9]+)>""").toList().map { it.toInt() }

    return Moon(id, Point3D(x, y, z), Point3D.ZERO)
}

private fun step(moons: List<Moon>): List<Moon> {
    return moons
        .cartesian(moons)
        .filterNot { (a, b) -> a == b }
        .groupingBy { it.first }
        .mapValues { it.second }
        .map { (moon, others) ->
            val velocityChange =
                others.map { calculateRelativeVelocity(moon, it) }.reduce(Point3D::plus)

            moon.applyVelocity(velocityChange)
        }
        .toList()
}

private fun calculateRelativeVelocity(moon: Moon, other: Moon): Point3D {
    fun forAxis(axis: KProperty1<Point3D, Int>) =
        when {
            axis.get(moon.position) > axis.get(other.position) -> -1
            axis.get(moon.position) < axis.get(other.position) -> +1
            else -> 0
        }

    return Point3D(forAxis(Point3D::x), forAxis(Point3D::y), forAxis(Point3D::z))
}

private data class AxisFingerprint(val positions: List<Int>, val velocities: List<Int>)

private fun fingerprintAxis(moons: List<Moon>, axis: KProperty1<Point3D, Int>): AxisFingerprint {
    val sorted = moons.sortedBy { it.id }

    return AxisFingerprint(
        sorted.map { it.position }.map { axis.get(it) },
        sorted.map { it.velocity }.map { axis.get(it) },
    )
}

fun main() {
    val input = readResourceAsString("/day12.txt")
    val parsed = input.lines().filterNotBlank().withIndex().map { (idx, it) -> parseMoon(idx, it) }

    part1 {
        val result = reduceTimes(1000, parsed) { step(it) }

        result.sumOf { it.energy }
    }

    part2 {
        val initialXFingerprint = fingerprintAxis(parsed, Point3D::x)
        val initialYFingerprint = fingerprintAxis(parsed, Point3D::y)
        val initialZFingerprint = fingerprintAxis(parsed, Point3D::z)

        var xRepeat: Long? = null
        var yRepeat: Long? = null
        var zRepeat: Long? = null

        var current = parsed
        repeatUntilTrue {
            val currentIteration = it.toLong() + 1
            current = step(current)

            if (fingerprintAxis(current, Point3D::x) == initialXFingerprint) {
                xRepeat = currentIteration
            }
            if (fingerprintAxis(current, Point3D::y) == initialYFingerprint) {
                yRepeat = currentIteration
            }
            if (fingerprintAxis(current, Point3D::z) == initialZFingerprint) {
                zRepeat = currentIteration
            }

            xRepeat != null && yRepeat != null && zRepeat != null
        }

        xRepeat!!.lcm(yRepeat!!).lcm(zRepeat!!)
    }
}
