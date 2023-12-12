import SpringStatus.*
import utils.*

private enum class SpringStatus {
    OK,
    BROKEN,
    UNKNOWN
}

private fun parseSpringLine(line: String) =
    line.map {
        when (it) {
            '.' -> OK
            '#' -> BROKEN
            '?' -> UNKNOWN
            else -> unreachable("char $it")
        }
    }

private data class SpringRecord(val springLine: List<SpringStatus>, val groups: List<Int>) {
    fun unfold() =
        SpringRecord(
            (0 until 5).flatMap { springLine + UNKNOWN }.dropLast(1),
            (0 until 5).flatMap { groups })

    fun markBroken(range: IntRange) =
        this.copy(
            springLine =
                springLine.mapIndexedNotNull { idx, elem ->
                    when {
                        idx < range.last + 2 -> null
                        else -> elem
                    }
                },
            groups = groups.drop(1))
}

private fun parseSpringRecord(line: String) =
    line
        .twoParts(" ")
        .mapFirst { parseSpringLine(it) }
        .mapSecond { it.split(",").mapInts() }
        .let { (a, b) -> SpringRecord(a, b) }

private fun solve(record: SpringRecord): Long {
    val memo = mutableMapOf<SpringRecord, Long>()

    fun inner(record: SpringRecord): Long {
        if (memo.contains(record)) {
            return memo[record]!!
        }

        if (record.groups.isEmpty()) {
            if (record.springLine.none { it == BROKEN }) {
                memo[record] = 1
                return 1
            } else {
                memo[record] = 0
                return 0
            }
        }

        val toBePlaced = record.groups.first()

        val candidateChunks =
            record.springLine
                .zipWithIndex()
                .chunksSatisfying { it.elem == UNKNOWN || it.elem == BROKEN }
                .takeWhileInclusive { chunk -> chunk.all { it.elem == UNKNOWN || it.elem == OK } }
                .filter { it.size >= toBePlaced }
                .toList()
        if (candidateChunks.isEmpty()) {
            return 0
        }

        val next =
            candidateChunks.flatMap { candidateChunk ->
                val narrowedChunkEndIndex =
                    candidateChunk
                        .chunksSatisfying { it.elem == BROKEN }
                        .map { chunk -> chunk.last().index + (toBePlaced - chunk.size) }
                        .firstOrNull()
                        ?.coerceAtMost(candidateChunk.last().index) ?: candidateChunk.last().index

                val narrowedChunk =
                    record.springLine
                        .zipWithIndex()
                        .subList(candidateChunk.first().index, narrowedChunkEndIndex + 1)

                narrowedChunk.windowed(toBePlaced, 1).mapNotNull {
                    if (record.springLine.getOrNull(it.last().index + 1) == BROKEN) {
                        return@mapNotNull null
                    }
                    record.markBroken(it.first().index..it.last().index)
                }
            }

        val result = next.sumOf { inner(it) }
        memo[record] = result
        return result
    }

    return inner(record)
}

fun main() {
    part1 {
        val input = readResourceAsString("/day12.txt")
        val records = input.parseLines { parseSpringRecord(it) }

        records.sumOf { solve(it) }
    }

    part2 {
        val input = readResourceAsString("/day12.txt")
        val records = input.parseLines { parseSpringRecord(it) }.map { it.unfold() }

        records.sumOf { solve(it) }
    }
}
