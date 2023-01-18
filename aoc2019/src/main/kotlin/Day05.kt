import intcode.IntcodeInterpreter
import intcode.parseIntcodeProgram
import utils.part1
import utils.part2
import utils.readResourceAsString

fun main() {
    val input = readResourceAsString("/day05.txt")
    val parsed = parseIntcodeProgram(input)

    part1 {
        val interpreter = IntcodeInterpreter(parsed, listOf(1))
        interpreter.run()

        interpreter.output.last()
    }
    part2 {
        val interpreter = IntcodeInterpreter(parsed, listOf(5))
        interpreter.run()

        interpreter.output.single()
    }
}
