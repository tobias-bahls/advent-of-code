import utils.cartesian
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toTriple

fun main() {
    val input = readResourceAsString("/day01.txt")

    val parsed = input.parseLines { it.toInt() }

    part1 {
        val (a, b) =
            parsed.cartesian(parsed).find { (a, b) -> a + b == 2020 }
                ?: error("Could not find two values summing to 2020")

        a * b
    }

    part2 {
        val (a, b, c) =
            parsed
                .cartesian(parsed)
                .cartesian(parsed)
                .map { it.toTriple() }
                .find { (a, b, c) -> a + b + c == 2020 }
                ?: error("Could not find three values summing to 2020")

        a * b * c
    }
}
