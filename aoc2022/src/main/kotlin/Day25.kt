import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import utils.Test
import utils.filterNotBlank
import utils.mapFirst
import utils.parseLines
import utils.part1
import utils.pow
import utils.readResourceAsString
import utils.runTests
import utils.toPair

private fun snafuDigitToBase10(char: Char): Long =
    when (char) {
        '0' -> 0
        '1' -> 1
        '2' -> 2
        '-' -> -1
        '=' -> -2
        else -> error("Unknown snafu digit: $char")
    }

private fun parseSnafuNumber(input: String): Long {
    return input
        .reversed()
        .mapIndexed { index, char -> 5L.pow(index) * snafuDigitToBase10(char) }
        .sum()
}

private fun toSnafuNumber(input: Long): String {
    var current = input
    var result = ""
    while (current > 0) {
        val (next, digit) =
            when (val mod = current % 5) {
                in (0..2) -> current / 5 to mod.toString()
                3L -> (current + 5) / 5 to "="
                4L -> (current + 5) / 5 to "-"
                else -> error("Unreachable: $mod")
            }

        current = next
        result += digit
    }

    return result.reversed()
}

fun main() {
    runTests()
    val input = readResourceAsString("/day25.txt")

    part1 { input.parseLines { parseSnafuNumber(it) }.sum().let { toSnafuNumber(it) } }
}

private val samples =
    """
        1              1
        2              2
        3             1=
        4             1-
        5             10
        6             11
        7             12
        8             2=
        9             2-
       10             20
       15            1=0
       20            1-0
     2022         1=11-2
    12345        1-0---0
314159265  1121-1110-1=0
   """
        .trimIndent()
        .lines()
        .map { line -> line.split(" ").filterNotBlank() }
        .map { line -> line.toPair().mapFirst { it.toLong() } }

@Test
private fun parseSnafuNumber() {
    samples.forEach { (expected, input) ->
        withClue("parseSnafuNumber($input)") { parseSnafuNumber(input) shouldBe expected }
    }
}

@Test
private fun toSnafuNumber() {
    samples.forEach { (input, expected) ->
        withClue("toSnafuNumber($input)") { toSnafuNumber(input) shouldBe expected }
    }
}
