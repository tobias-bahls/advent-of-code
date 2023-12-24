import java.math.BigDecimal
import utils.*

private data class BigDecimalPoint3D(val x: BigDecimal, val y: BigDecimal, val z: BigDecimal) {
    fun translate(translation: BigDecimalPoint3D): BigDecimalPoint3D {
        return BigDecimalPoint3D(
            this.x + translation.x,
            this.y + translation.y,
            this.z + translation.z,
        )
    }

    operator fun plus(other: BigDecimalPoint3D) = translate(other)
}

private fun parseBigDecimalPoint3D(input: String) =
    input
        .match("""(-?\d+),\s*(-?\d+),\s*(-?\d+)""")
        .toList()
        .map { it.toBigDecimal().setScale(8) }
        .let { (x, y, z) -> BigDecimalPoint3D(x, y, z) }

private data class Hailstone(val start: BigDecimalPoint3D, val velocity: BigDecimalPoint3D) {
    fun intersects2D(other: Hailstone, validRange: ClosedRange<BigDecimal>): Boolean {
        val den = this.velocity.x / this.velocity.y - other.velocity.x / other.velocity.y
        if (den.compareTo(BigDecimal.ZERO) == 0) {
            return false
        }
        val iY =
            ((other.start.x - (other.velocity.x / other.velocity.y * other.start.y)) +
                (this.velocity.x / this.velocity.y * this.start.y) - this.start.x) / den
        val iX = ((iY - this.start.y) / this.velocity.y) * this.velocity.x + this.start.x

        val inFutureX =
            (iX - this.start.x).signum() == this.velocity.x.signum() &&
                (iX - other.start.x).signum() == other.velocity.x.signum()
        val inFutureY =
            (iY - this.start.y).signum() == this.velocity.y.signum() &&
                (iY - other.start.y).signum() == other.velocity.y.signum()

        return (inFutureX && inFutureY) && iX in validRange && iY in validRange
    }
}

fun main() {
    part1 {
        val validRange = 200000000000000.toBigDecimal()..400000000000000.toBigDecimal()
        val input = readResourceAsString("/day24.txt")

        val hailstones =
            input.parseLines { line ->
                line
                    .split(" @ ")
                    .map { parseBigDecimalPoint3D(it) }
                    .let { (speed, velocity) -> Hailstone(speed, velocity) }
            }

        hailstones.zipWithIndex().sumOf { (a, idx) ->
            hailstones.drop(idx).count { b -> a.intersects2D(b, validRange) }
        }
    }

    part2 {
        val input = readResourceAsString("/day24.txt")

        val hailstones =
            input.parseLines { line ->
                line
                    .split(" @ ")
                    .map { parseBigDecimalPoint3D(it) }
                    .let { (speed, velocity) -> Hailstone(speed, velocity) }
            }

        z3 {
            val x = real("x")
            val y = real("y")
            val z = real("z")

            val vx = real("vx")
            val vy = real("vy")
            val vz = real("vz")

            hailstones.forEachIndexed { i, stone ->
                val ti = ctx.mkRealConst("t_${i}")

                solver.add(stone.start.x.real + (stone.velocity.x.real * ti) eq (x + vx * ti))
                solver.add(stone.start.y.real + (stone.velocity.y.real * ti) eq (y + vy * ti))
                solver.add(stone.start.z.real + (stone.velocity.z.real * ti) eq (z + vz * ti))
            }

            solver.checkAndAssert()

            val model = solver.model
            val solvedX = model[x].intoBigDecimal()
            val solvedY = model[y].intoBigDecimal()
            val solvedZ = model[z].intoBigDecimal()

            solvedX + solvedY + solvedZ
        }
    }
}
