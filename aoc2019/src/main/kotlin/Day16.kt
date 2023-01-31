import kotlin.math.absoluteValue
import utils.cycleGet
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.reduceTimes

private class Multiples(size: Int) {
    private val patternTemplate = listOf(0, 1, 0, -1)
    private val patternsPerOutputDigit: List<List<Int>>

    init {
        this.patternsPerOutputDigit =
            (0..size).map { outputIndex ->
                val template = patternTemplate.flatMap { num -> List(outputIndex + 1) { num } }

                template
            }
    }

    fun multipleAt(inputIndex: Int, outputDigitIndex: Int): Int {
        val pattern = patternsPerOutputDigit[outputDigitIndex]

        return pattern.cycleGet(inputIndex + 1)
    }
}

private fun executePhasePart1(digits: List<Int>, multiples: Multiples): List<Int> {
    return digits.indices
        .map { outputIndex ->
            digits.withIndex().sumOf { (inputIndex, i) ->
                i * multiples.multipleAt(inputIndex, outputIndex)
            }
        }
        .map { (it % 10).absoluteValue }
}

private fun executePhasePart2(digits: List<Int>): List<Int> {
    return digits.reversed().runningReduce(Int::plus).map { it % 10 }.reversed()
}

fun main() {
    val input = readResourceAsString("/day16.txt")
    val parsed = input.trim().map { it.digitToInt() }

    part1 {
        val multiples = Multiples(parsed.size)
        reduceTimes(100, parsed) { executePhasePart1(it, multiples) }.take(8).joinToString("")
    }

    part2 {
        val offset = parsed.take(7).joinToString("").toInt()

        val repeated = List(10000) { parsed }.flatten()
        val relevant = repeated.subList(offset, repeated.size)
        reduceTimes(100, relevant) { executePhasePart2(it) }.take(8).joinToString("")
    }
}
