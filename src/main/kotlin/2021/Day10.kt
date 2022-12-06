package `2021`

import filterNotBlank
import middleElement
import part1
import part2
import readResourceAsString

enum class CharacterType(val open: Char, val close: Char) {
    PAREN('(', ')'),
    SQUARE('[', ']'),
    CURLY('{', '}'),
    ANGLE('<', '>');

    companion object {
        fun parse(input: Char): CharacterType {
            return values().first { it.open == input || it.close == input }
        }
    }
}

data class ChunkCharacter(val char: Char) {
    val type
        get() = CharacterType.parse(char)

    val open
        get() = type.open == char

    val close
        get() = type.close == char
}

sealed interface ValidationResult {
    object Success : ValidationResult

    data class Invalid(val char: ChunkCharacter, val index: Int) : ValidationResult
    data class Incomplete(val missing: List<ChunkCharacter>) : ValidationResult
}

fun validate(input: String): ValidationResult {
    val stack = mutableListOf<ChunkCharacter>()
    input
        .toCharArray()
        .map { ChunkCharacter(it) }
        .forEachIndexed { index, it ->
            val expectedType = stack.lastOrNull()?.type
            when {
                it.open -> stack += it
                it.close && it.type == expectedType -> stack.removeLast()
                it.close && it.type != expectedType -> return ValidationResult.Invalid(it, index)
            }
        }

    if (stack.isNotEmpty()) {
        return ValidationResult.Incomplete(stack.reversed())
    }

    return ValidationResult.Success
}

fun main() {
    val input = readResourceAsString("2021/day10.txt")

    val validated = input.lines().filterNotBlank().map { validate(it) }

    part1 {
        validated.filterIsInstance<ValidationResult.Invalid>().sumOf {
            when (it.char.type) {
                CharacterType.PAREN -> 3
                CharacterType.SQUARE -> 57
                CharacterType.CURLY -> 1197
                CharacterType.ANGLE -> 25137L
            }
        }
    }
    part2 {
        validated
            .filterIsInstance<ValidationResult.Incomplete>()
            .map { result ->
                result.missing.fold(0L) { acc, it ->
                    val thisScore =
                        when (it.type) {
                            CharacterType.PAREN -> 1
                            CharacterType.SQUARE -> 2
                            CharacterType.CURLY -> 3
                            CharacterType.ANGLE -> 4L
                        }

                    acc * 5 + thisScore
                }
            }
            .sorted()
            .middleElement()
    }
}
