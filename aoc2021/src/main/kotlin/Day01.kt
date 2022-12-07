import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString

fun main() {
    val input = readResourceAsString("/day01.txt").parseLines { it.toInt() }

    part1 { input.windowed(2).count { (a, b) -> b > a } }

    part2 { input.windowed(3).windowed(2).count { (a, b) -> b.sum() > a.sum() } }
}
