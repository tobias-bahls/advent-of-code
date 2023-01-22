import intcode.IntcodeInterpreter
import intcode.parseIntcodeProgram
import utils.cartesian
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.updated

fun setProgramInput(program: List<Long>, noun: Long, verb: Long) =
    program.updated(1, noun).updated(2, verb)

fun main() {
    val input = readResourceAsString("/day02.txt")
    val program = parseIntcodeProgram(input)

    part1 {
        val updatedProgram = setProgramInput(program, 12, 2)
        val interpreter = IntcodeInterpreter(updatedProgram)
        interpreter.run()

        interpreter.getMemory(0)
    }
    part2 {
        (0L..99L)
            .cartesian(0L..99L)
            .find { (a, b) ->
                val modifiedProgram = setProgramInput(program, a, b)
                val interpreter = IntcodeInterpreter(modifiedProgram)

                interpreter.run()

                interpreter.getMemory(0) == 19690720L
            }
            ?.let { (a, b) -> 100 * a + b }
    }
}
