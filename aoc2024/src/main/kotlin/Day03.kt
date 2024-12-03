import utils.*

fun main() {
    part1 {
        val input = readResourceAsString("/day04.txt")
        Regex("mul\\((\\d{1,3}),(\\d{1,3})\\)").findAll(input).sumOf { result ->
            val (a, b) = result.destructured.toPair().map { it.toInt() }
            a * b
        }
    }

    part2 {
        val input = readResourceAsString("/day04.txt")

        data class State(val enabled: Boolean = true, val accumulator: Int = 0)
        Regex("mul\\((\\d{1,3}),(\\d{1,3})\\)|do\\(\\)|don't\\(\\)")
            .findAll(input)
            .fold(State()) { state, result ->
                when {
                    state.enabled && result.value.startsWith("mul") -> {
                        val (a, b) = result.destructured.toPair().map { it.toInt() }
                        state.copy(accumulator = state.accumulator + a * b)
                    }
                    !state.enabled && result.value.startsWith("mul") -> {
                        state
                    }
                    result.value.startsWith("don't") -> {
                        state.copy(enabled = false)
                    }
                    result.value.startsWith("do") -> {
                        state.copy(enabled = true)
                    }
                    else -> error("Invalid value: ${result.value}")
                }
            }
            .accumulator
    }
}
