import utils.mapFirst
import utils.match
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.twoParts

private data class PasswordPolicy(val firstNumber: Int, val secondNumber: Int, val char: Char) {
    fun isValidPart1(password: String) =
        (firstNumber..secondNumber).contains(password.count { it == char })

    fun isValidPart2(password: String) =
        (password[firstNumber - 1] == char) xor (password[secondNumber - 1] == char)
}

private fun parsePasswordPolicy(input: String): PasswordPolicy {
    val (atLeast, atMost, char) = input.match("""(\d+)-(\d+) ([a-z])""")

    return PasswordPolicy(atLeast.toInt(), atMost.toInt(), char.firstOrNull()!!)
}

private data class CorruptedPassword(val policy: PasswordPolicy, val password: String) {
    val validPart1
        get() = policy.isValidPart1(password)

    val validPart2
        get() = policy.isValidPart2(password)
}

private fun parseCorruptedPassword(input: String): CorruptedPassword {
    val (policy, password) = input.twoParts(":").mapFirst { parsePasswordPolicy(it) }

    return CorruptedPassword(policy, password)
}

fun main() {
    val input = readResourceAsString("/day02.txt")

    val parsed = input.parseLines { parseCorruptedPassword(it) }

    part1 { parsed.count { it.validPart1 } }
    part2 { parsed.count { it.validPart2 } }
}
