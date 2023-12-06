import utils.*

fun main() {
    val input = readResourceAsString("/day02.txt")
    part1 {
        val threes = input.lines().count { it.toFrequencyMap().any { (_, v) -> v == 3 } }
        val twos = input.lines().count { it.toFrequencyMap().any { (_, v) -> v == 2 } }

        threes * twos
    }

    part2 {
        val found =
            input.lines().cartesian(input.lines()).find { (a, b) ->
                a.zip(b).count { (aChar, bChar) -> aChar != bChar } == 1
            } ?: error("Could not find matching boxes")

        found.let { (a, b) ->
            a.zip(b)
                .filter { (aChar, bChar) -> aChar == bChar }
                .joinToString("") { it.first.toString() }
        }
    }
}
