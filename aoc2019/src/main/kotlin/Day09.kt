import intcode.IntcodeInterpreter
import intcode.parseIntcodeProgram
import utils.part1
import utils.part2
import utils.readResourceAsString

fun main() {
    val input = readResourceAsString("/day09.txt")
    val parsed = parseIntcodeProgram(input)

    part1 {
        val interpreter = IntcodeInterpreter(parsed, listOf(1))
        interpreter.run()

        interpreter.fullOutput.single()
    }
    part2 {
        val interpreter = IntcodeInterpreter(parsed, listOf(2))
        interpreter.run()

        interpreter.fullOutput.single()
    }
}
