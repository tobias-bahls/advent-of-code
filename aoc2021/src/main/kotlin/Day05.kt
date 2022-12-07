import datastructures.Point
import kotlin.math.abs
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString

data class Line(val start: Point, val end: Point) {
    fun points(): List<Point> {
        val difference = Point(end.x - start.x, end.y - start.y)

        val distance =
            when {
                difference.x == 0 -> difference.y
                difference.y == 0 -> difference.x
                else -> difference.x
            }.let { abs(it) }

        return (0..distance).map { step ->
            val dx =
                when {
                    difference.x == 0 -> 0
                    difference.x > 0 -> 1
                    difference.x < 0 -> -1
                    else -> error("Unreachable: ${difference.x}")
                }

            val dy =
                when {
                    difference.y == 0 -> 0
                    difference.y > 0 -> 1
                    difference.y < 0 -> -1
                    else -> error("Unreachable: ${difference.y}")
                }

            Point(start.x + step * dx, start.y + step * dy)
        }
    }
}

fun main() {
    val input =
        readResourceAsString("/day05.txt").parseLines { line ->
            line
                .split(" -> ")
                .map {
                    it.split(",").let { (x, y) -> Point(Integer.parseInt(x), Integer.parseInt(y)) }
                }
                .let { (start, end) -> Line(start, end) }
        }

    part1 {
        input
            .filter { it.start.x == it.end.x || it.start.y == it.end.y }
            .flatMap { it.points() }
            .groupingBy { it }
            .eachCount()
            .filterValues { it >= 2 }
            .count()
    }

    part2 {
        input.flatMap { it.points() }.groupingBy { it }.eachCount().filterValues { it >= 2 }.count()
    }
}
