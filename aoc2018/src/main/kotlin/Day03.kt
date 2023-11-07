import datastructures.Point2D
import datastructures.Rectangle
import utils.*

private data class Day03Claim(val id: String, val rect: Rectangle)

private fun parseClaim(str: String): Day03Claim {
    val (claim, x, y, w, h) = str.match("#(\\d+) @ (\\d+),(\\d+): (\\d+)x(\\d+)")

    return Day03Claim(
        id = claim,
        rect =
            Rectangle(
                position = Point2D(x.toInt(), y.toInt()), width = w.toInt(), height = h.toInt()))
}

fun main() {
    val sample =
        """
        #1 @ 1,3: 4x4
        #2 @ 3,1: 4x4
        #3 @ 5,5: 2x2
    """
            .trimIndent()

    val input = readResourceAsString("/day03.txt")
    val claims = input.parseLines { parseClaim(it) }

    fun getOverlappingPoints(claims: List<Day03Claim>): Set<Point2D> {
        val marked = Counter<Point2D>()
        claims.map { claim -> marked.increment(claim.rect.points) }
        return marked.counts.filterValues { it >= 2 }.keys
    }

    part1 { getOverlappingPoints(claims).size }

    part2 {
        val overlappingPoints = getOverlappingPoints(claims)
        val claim =
            claims.find { claim -> claim.rect.points.all { it !in overlappingPoints } }
                ?: error("could not find non-overlapping claim")

        claim.id
    }
}
