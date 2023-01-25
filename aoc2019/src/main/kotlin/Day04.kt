import utils.consecutiveEqualElements
import utils.parseClosedIntRange
import utils.part1
import utils.part2
import utils.readResourceAsString

private fun isValidPasswordPart1(number: Int): Boolean {
    val digits = number.toString().toList().map { it.digitToInt() }

    return when {
        !isAllIncreasingOrSame(digits) -> false
        !digits.windowed(2).any { (a, b) -> a == b } -> false
        else -> true
    }
}

private fun isValidPasswordPart2(number: Int): Boolean {
    val digits = number.toString().toList().map { it.digitToInt() }

    return when {
        !isAllIncreasingOrSame(digits) -> false
        !digits.consecutiveEqualElements().any { it.size == 2 } -> false
        else -> true
    }
}

private fun isAllIncreasingOrSame(digits: List<Int>) = digits.windowed(2).all { (a, b) -> a <= b }

fun main() {
    val input = readResourceAsString("/day04.txt")
    val parsed = parseClosedIntRange(input.trim())

    part1 { parsed.count { isValidPasswordPart1(it) } }
    part2 { parsed.count { isValidPasswordPart2(it) } }
}
