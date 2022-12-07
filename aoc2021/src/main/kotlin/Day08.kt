import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString

val ALL_SEGMENTS = setOf('a', 'b', 'c', 'd', 'e', 'f', 'g')

val DIGIT_TO_SEGMENTS =
    mapOf(
        0 to setOf(0, 1, 2, 3, 4, 5),
        1 to setOf(1, 2),
        2 to setOf(0, 1, 3, 4, 6),
        3 to setOf(0, 1, 2, 3, 6),
        4 to setOf(1, 2, 5, 6),
        5 to setOf(0, 2, 3, 5, 6),
        6 to setOf(0, 2, 3, 4, 5, 6),
        7 to setOf(0, 1, 2),
        8 to setOf(0, 1, 2, 3, 4, 5, 6),
        9 to setOf(0, 1, 2, 3, 5, 6),
    )

data class Segments(val segments: List<Char>) {

    fun isUnambigous() = listOf(2, 3, 4, 7).contains(segments.size)

    fun getLitSegments(): Set<Int> {
        if (isUnambigous()) {
            return DIGIT_TO_SEGMENTS.values.find { it.size == segments.size }!!
        }

        return when (segments.size) {
            5 -> setOf(0, 3, 6) // 2,3,5 -> all a, d, g
            6 -> setOf(0, 2, 3, 5) // 0, 6, 9 -> all a, f, g, b
            else -> error("Unexpected size: ${segments.size}")
        }
    }

    fun invertedSegments() = ALL_SEGMENTS.subtract(this.segments)
}

data class SegmentGuess(var segments: List<Set<Char>> = List(7) { ALL_SEGMENTS }) {

    fun addSegment(seg: Segments) {
        val litSegments = seg.getLitSegments()

        segments =
            segments.mapIndexed { index, segmentPossibilites ->
                when {
                    litSegments.contains(index) -> segmentPossibilites.intersect(seg.segments)
                    seg.isUnambigous() -> segmentPossibilites.intersect(seg.invertedSegments())
                    else -> segmentPossibilites
                }
            }

        val solved = segments.filter { it.size == 1 }.map { it.first() }.toSet()
        segments =
            segments.map {
                if (it.size > 1) {
                    it.minus(solved)
                } else {
                    it
                }
            }
    }
}

data class Example(val data: List<Segments>, val output: List<Segments>)

fun main() {
    val input =
        readResourceAsString("/day08.txt").parseLines { example ->
            val (data, output) = example.split(" | ")

            Example(
                data = data.split(" ").map { Segments(it.toCharArray().toList()) },
                output = output.split(" ").map { Segments(it.toCharArray().toList()) },
            )
        }

    part1 { input.sumOf { e -> e.output.count { it.isUnambigous() } } }

    part2 {
        input.sumOf {
            val key =
                it.data.fold(SegmentGuess()) { guess, segments ->
                    guess.addSegment(segments)

                    guess
                }

            it.output
                .map {
                    val litSegments = it.segments.map { key.segments.indexOf(setOf(it)) }.toSet()
                    DIGIT_TO_SEGMENTS.filterValues { it == litSegments }.keys.first()
                }
                .joinToString("")
                .let { Integer.parseInt(it) }
        }
    }
}
