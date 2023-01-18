import intcode.IntcodeInterpreter
import intcode.parseIntcodeProgram
import utils.cartesian
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.updated

fun setProgramInput(program: List<Int>, noun: Int, verb: Int) =
    program.updated(1, noun).updated(2, verb)

fun main() {
    val input = readResourceAsString("/day02.txt")
    val program = parseIntcodeProgram(input)

    part1 {
        val updatedProgram = setProgramInput(program, 12, 2)
        val interpreter = IntcodeInterpreter(updatedProgram)
        interpreter.run()

        interpreter.memory[0]
    }
    part2 {
        (0..99)
            .cartesian(0..99)
            .find { (a, b) ->
                val modifiedProgram = setProgramInput(program, a, b)
                val interpreter = IntcodeInterpreter(modifiedProgram)

                interpreter.run()

                interpreter.memory[0] == 19690720
            }
            ?.let { (a, b) -> 100 * a + b }
    }
}
