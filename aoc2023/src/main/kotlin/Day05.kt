import algorithms.repeat
import utils.*

private data class CategoryMapping(
    val source: String,
    val dest: String,
    val mappings: List<RangeMapping>
) {
    fun map(num: Long) = mappings.mapNotNull { it.map(num) }.lastOrNull() ?: num

    fun map(range: LongRange) = mappings.mapNotNull { it.map(range) }.ifEmpty { listOf(range) }
}

private fun parseMapping(str: String) =
    str.split("\n").firstRest().let { (title, mappings) ->
        val (from, to) = title.match("(.*)-to-(.*) map").toPair()

        CategoryMapping(
            source = from,
            dest = to,
            mappings =
                mappings.map { mapping ->
                    val (toStart, fromStart, length) = mapping.split(" ").map { it.toLong() }

                    RangeMapping(toStart, fromStart, length)
                })
    }

private data class RangeMapping(val dest: LongRange, val source: LongRange) {

    constructor(
        toStart: Long,
        fromStart: Long,
        length: Long
    ) : this(toStart until toStart + length, fromStart until fromStart + length)

    val offset = dest.first - source.first

    fun map(num: Long) =
        when (num) {
            in source -> num + offset
            !in source -> null
            else -> unreachable()
        }

    fun map(range: LongRange) =
        when {
            range.first > source.last -> null
            range.last <= source.first -> null
            else -> range.fastIntersection(source).offset(offset)
        }
}

fun main() {
    part1 {
        val input = readResourceAsString("/day05.txt")

        val (seedsStr, mapsStr) = input.split("\n\n").firstRest()
        val seeds = seedsStr.twoParts(": ").second.split(" ").map { it.toLong() }
        val maps = mapsStr.map { parseMapping(it) }

        data class Step(val category: String, val ids: List<Long>)
        repeat<Step>(Step("seed", seeds)) { step ->
                if (step.category == "location") {
                    return@repeat stop()
                }
                val mapping =
                    maps.find { it.source == step.category }
                        ?: error("no mapping from ${step.category}")
                val mappedElements = step.ids.map { mapping.map(it) }

                next(Step(mapping.dest, mappedElements))
            }
            .element
            .ids
            .min()
    }

    part2 {
        val input = readResourceAsString("/day05.txt")

        val (seedsStr, mapsStr) = input.split("\n\n").firstRest()
        val seedRanges =
            seedsStr
                .twoParts(": ")
                .second
                .split(" ")
                .map { it.toLong() }
                .windowed(2, 2)
                .map { (start, length) -> (start until start + length) }
        val maps = mapsStr.map { parseMapping(it) }

        data class Step(val category: String, val ranges: List<LongRange>)
        repeat<Step>(Step("seed", seedRanges)) { step ->
                if (step.category == "location") {
                    return@repeat stop()
                }
                val mapping =
                    maps.find { it.source == step.category }
                        ?: error("no mapping from ${step.category}")
                val mappedElements = step.ranges.flatMap { mapping.map(it) }

                next(Step(mapping.dest, mappedElements))
            }
            .element
            .ranges
            .minOf { it.first }
    }
}
