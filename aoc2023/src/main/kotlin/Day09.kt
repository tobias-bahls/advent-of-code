import algorithms.produce
import utils.*

private fun extrapolate(readingHistory: List<Int>, combineSequences: (Int, List<Int>) -> Int): Int {
    val sequences =
        produce(readingHistory, includeInitial = true) { current ->
                if (current.all { it == 0 }) {
                    stop()
                } else {
                    next(current.windowed(2).map { (a, b) -> b - a })
                }
            }
            .elements

    return sequences.reversed().drop(1).fold(0) { difference, sequence ->
        combineSequences(difference, sequence)
    }
}

fun main() {
    part1 {
        val input = readResourceAsString("/day09.txt")
        val readings = input.parseLines { l -> l.split(" ").map { it.toInt() } }

        readings.sumOf { extrapolate(it) { difference, sequence -> sequence.last() + difference } }
    }

    part2 {
        val input = readResourceAsString("/day09.txt")
        val readings = input.parseLines { l -> l.split(" ").map { it.toInt() } }

        readings.sumOf { extrapolate(it) { difference, sequence -> sequence.first() - difference } }
    }
}
