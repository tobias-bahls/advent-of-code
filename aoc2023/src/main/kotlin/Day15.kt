import utils.*

private fun hash(str: String): Int {
    return str.fold(0) { acc, char -> ((acc + char.code) * 17) % 256 }
}

private sealed interface Day15Operation {
    data class Put(val label: String, val focalLength: Int) : Day15Operation

    data class Remove(val label: String) : Day15Operation
}

private fun parseDay15Operation(str: String) =
    if ('=' in str) {
        parsePutOperation(str)
    } else {
        parseRemoveOperation(str)
    }

private fun parsePutOperation(str: String) =
    str.twoParts('=')
        .mapSecond { it.toInt() }
        .let { (label, focalLength) -> Day15Operation.Put(label, focalLength) }

private fun parseRemoveOperation(str: String) = str.trim('-').let { Day15Operation.Remove(it) }

fun main() {
    part1 {
        val input = readResourceAsString("/day15.txt")
        input.split(",").sumOf { hash(it) }
    }

    part2 {
        val input = readResourceAsString("/day15.txt")

        data class LensData(val label: String, val focalLength: Int)

        val map = mutableMapOf<Int, List<LensData>>()
        val ops = input.split(",").map { parseDay15Operation(it) }

        ops.forEach { op ->
            when (op) {
                is Day15Operation.Put -> {
                    val bucket = hash(op.label)
                    val existing = map.getOrDefault(bucket, emptyList())
                    val isPresent = existing.any { it.label == op.label }
                    val newList =
                        if (isPresent) {
                            existing.map {
                                if (op.label == it.label) {
                                    it.copy(focalLength = op.focalLength)
                                } else {
                                    it
                                }
                            }
                        } else {
                            existing + LensData(op.label, op.focalLength)
                        }
                    map[bucket] = newList
                }
                is Day15Operation.Remove -> {
                    val bucket = hash(op.label)
                    val existing = map.getOrDefault(bucket, emptyList())
                    val newList =
                        existing.mapNotNull {
                            if (it.label == op.label) {
                                null
                            } else {
                                it
                            }
                        }
                    map[bucket] = newList
                }
            }
        }

        map.entries.sumOf { (box, contents) ->
            (box + 1) * contents.zipWithIndex().sumOf { (it.index + 1) * it.elem.focalLength }
        }
    }
}
