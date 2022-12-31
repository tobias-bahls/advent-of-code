import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.transpose

fun main() {
    val input =
        readResourceAsString("/day03.txt").parseLines { it.split("").filter { it.isNotBlank() } }

    part1 {
        input
            .transpose()
            .joinToString("") { x ->
                val ones = x.count { it == "1" }
                val zeroes = x.count { it == "0" }

                if (ones > zeroes) {
                    "1"
                } else {
                    "0"
                }
            }
            .let {
                val gamma = Integer.parseInt(it, 2)
                val epsilon =
                    it.replace(Regex(".")) { r ->
                            when (r.value) {
                                "0" -> "1"
                                "1" -> "0"
                                else -> error("Invalid: ${r.value}")
                            }
                        }
                        .let { Integer.parseInt(it, 2) }

                gamma * epsilon
            }
    }

    fun executePart2(mostCommon: Boolean): Int {
        return (0..input.first().size)
            .fold(input) { result, index ->
                if (result.size == 1) {
                    return@fold result
                }

                val ones = result.count { it[index] == "1" }
                val zeroes = result.count { it[index] == "0" }

                val keep =
                    when {
                        mostCommon && ones >= zeroes -> "1"
                        mostCommon && ones < zeroes -> "0"
                        !mostCommon && ones < zeroes -> "1"
                        !mostCommon && ones >= zeroes -> "0"
                        else -> error("Unreachable")
                    }

                result.filter { it[index] == keep }
            }
            .first()
            .joinToString("")
            .let { Integer.parseInt(it, 2) }
    }

    part2 { executePart2(mostCommon = true) * executePart2(mostCommon = false) }
}
