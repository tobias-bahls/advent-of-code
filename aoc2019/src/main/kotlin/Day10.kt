import datastructures.parseAsciiPointGrid
import utils.cycle
import utils.part1
import utils.part2
import utils.readResourceAsString

fun main() {
    val input = readResourceAsString("/day10.txt")
    val parsed = parseAsciiPointGrid(input.trim().lines())

    part1 { parsed.maxOf { asteroid -> parsed.map { asteroid.angleTo(it) }.distinct().size } }
    part2 {
        val station =
            parsed.maxBy { asteroid ->
                parsed.filter { it != asteroid }.map { asteroid.angleTo(it) }.distinct().size
            }

        val angleMap =
            parsed
                .groupBy { station.angleTo(it) }
                .mapValues { (_, v) -> v.sortedBy { it.distanceTo(station) }.toMutableList() }

        val angles = angleMap.keys.sorted().cycle().iterator()

        var numDestroyed = 0
        while (true) {
            val angle = angles.next()
            val remainingAsteroidsAtAngle = angleMap.getValue(angle)
            if (remainingAsteroidsAtAngle.isEmpty()) {
                continue
            }

            val destroyed = remainingAsteroidsAtAngle.removeFirst()
            numDestroyed++
            if (numDestroyed == 200) {
                return@part2 destroyed.x * 100 + destroyed.y
            }
        }
    }
}
