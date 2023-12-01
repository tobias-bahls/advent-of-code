import utils.part1
import utils.part2
import utils.readResourceAsString

fun main() {
    part1 {
        readResourceAsString("/day01.txt").lines().sumOf { line ->
            val first = line.find { it.isDigit() } ?: error("Could not find first digit")
            val last = line.findLast { it.isDigit() } ?: error("Could not find last digit")

            (first.digitToInt() * 10) + last.digitToInt()
        }
    }

    part2 {
        val numberMap =
            mapOf(
                "one" to 1,
                "two" to 2,
                "three" to 3,
                "four" to 4,
                "five" to 5,
                "six" to 6,
                "seven" to 7,
                "eight" to 8,
                "nine" to 9,
            )

        val numbers = numberMap.keys + numberMap.values.map { it.toString() }

        data class NumberPosition(val index: Int, val number: String)
        fun digitToNumber(str: String): Int =
            if (str.length == 1) str.single().digitToInt() else numberMap.getValue(str)

        readResourceAsString("/day01.txt").lines().sumOf { line ->
            val first =
                numbers
                    .mapNotNull {
                        val index = line.indexOf(it)
                        if (index == -1) {
                            null
                        } else {
                            NumberPosition(index, line.substring(index, index + it.length))
                        }
                    }
                    .minBy { it.index }

            val last =
                numbers
                    .mapNotNull {
                        val index = line.lastIndexOf(it)
                        if (index == -1) {
                            null
                        } else {
                            NumberPosition(index, line.substring(index, index + it.length))
                        }
                    }
                    .maxBy { it.index }

            (digitToNumber(first.number) * 10) + digitToNumber(last.number)
        }
    }
}
