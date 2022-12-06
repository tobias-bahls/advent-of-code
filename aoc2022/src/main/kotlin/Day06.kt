import utils.part1
import utils.part2
import utils.readResourceAsString

fun solve(input: String, markerLength: Int): Int {
    val markerIndex =
        input.windowed(markerLength, 1).indexOfFirst {
            it.toCharArray().distinct().size == markerLength
        }

    return markerIndex + markerLength
}

fun main() {
    val input = readResourceAsString("/day06.txt")

    part1 { solve(input, 4) }

    part2 { solve(input, 14) }
}
