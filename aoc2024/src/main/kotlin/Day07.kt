import utils.*

data class Day07CalibrationEquation(val testValue: Long, val values: List<Long>)

data class Day07PartiallySolvedCalibrationEquation(
    val testValue: Long,
    val current: Long,
    val remainingValues: List<Long>
) {
    val done = remainingValues.isEmpty()
    val solved = testValue == current && done
    val impossible = current > testValue
}

fun main() {
    part1 {
        fun solve(equation: Day07PartiallySolvedCalibrationEquation): Boolean {
            when {
                equation.solved -> return true
                equation.done -> return false
                equation.impossible -> return false
            }

            val nextValue = equation.remainingValues.first()
            val remaining = equation.remainingValues.drop(1)

            val plus =
                Day07PartiallySolvedCalibrationEquation(
                    testValue = equation.testValue,
                    current = equation.current + nextValue,
                    remainingValues = remaining)
            val mult =
                Day07PartiallySolvedCalibrationEquation(
                    testValue = equation.testValue,
                    current = equation.current * nextValue,
                    remainingValues = remaining)

            return solve(plus) || solve(mult)
        }
        val input = readResourceAsString("/day07.txt")
        input
            .parseLines { l ->
                val (testValue, values) =
                    l.twoParts(":").mapFirst { it.toLong() }.mapSecond { it.split(" ").mapLongs() }

                Day07CalibrationEquation(testValue, values)
            }
            .filter {
                solve(
                    Day07PartiallySolvedCalibrationEquation(
                        testValue = it.testValue,
                        current = it.values.first(),
                        remainingValues = it.values.drop(1)))
            }
            .sumOf { it.testValue }
    }

    part2 {
        fun solve(equation: Day07PartiallySolvedCalibrationEquation): Boolean {
            when {
                equation.solved -> return true
                equation.done -> return false
                equation.impossible -> return false
            }

            val nextValue = equation.remainingValues.first()
            val remaining = equation.remainingValues.drop(1)

            val plus =
                Day07PartiallySolvedCalibrationEquation(
                    testValue = equation.testValue,
                    current = equation.current + nextValue,
                    remainingValues = remaining)
            val mult =
                Day07PartiallySolvedCalibrationEquation(
                    testValue = equation.testValue,
                    current = equation.current * nextValue,
                    remainingValues = remaining)
            val concat =
                Day07PartiallySolvedCalibrationEquation(
                    testValue = equation.testValue,
                    current = "${equation.current}${nextValue}".toLong(),
                    remainingValues = remaining)

            return solve(plus) || solve(mult) || solve(concat)
        }
        val input = readResourceAsString("/day07.txt")
        input
            .parseLines { l ->
                val (testValue, values) =
                    l.twoParts(":").mapFirst { it.toLong() }.mapSecond { it.split(" ").mapLongs() }

                Day07CalibrationEquation(testValue, values)
            }
            .filter {
                solve(
                    Day07PartiallySolvedCalibrationEquation(
                        testValue = it.testValue,
                        current = it.values.first(),
                        remainingValues = it.values.drop(1)))
            }
            .sumOf { it.testValue }
    }
}
