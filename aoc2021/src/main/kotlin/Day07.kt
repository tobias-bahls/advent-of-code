import kotlin.math.absoluteValue
import kotlin.math.floor
import utils.mean
import utils.median
import utils.part1
import utils.part2
import utils.readResourceAsString

fun main() {
    val input: List<Int> =
        readResourceAsString("/day07.txt")
            .lines()
            .first { it.isNotBlank() }
            .split(",")
            .map { Integer.parseInt(it) }

    part1 { input.sumOf { (it - input.median()).absoluteValue } }

    part2 {
        input.sumOf {
            val mid = floor(input.mean()).toInt()
            val dist = (it - mid).absoluteValue

            dist * (dist + 1) / 2
        }
    }
}
