import intcode.IntcodeInterpreter
import intcode.interact
import intcode.parseIntcodeProgram
import utils.*

fun main() {
    part1 {
        val robotProgram =
            """
            NOT C T
            OR T J
            NOT A T
            OR T J
            AND D J
        """
                .trimIndent()

        runSpringcodeProgram(robotProgram, "WALK")
    }

    part2 {
        val robotProgram =
            """
            NOT C T
            OR T J
            NOT A T
            OR T J
            NOT B T
            OR T J
            AND D J
            OR E T
            OR H T
            AND T J
        """
                .trimIndent()

        runSpringcodeProgram(robotProgram, "RUN")
    }
}

private fun runSpringcodeProgram(robotProgram: String, trigger: String): Long {
    val program = parseIntcodeProgram(readResourceAsString("/day21.txt"))
    val interp = IntcodeInterpreter(program)

    interp.interact {
        waitForInput()
        expectAsciiOutput("Input instructions:\n")
        writeLine(robotProgram)
        writeLine(trigger)

        expectOutput("last output should be outside of ascii range") { it.last() > 255 }
    }

    return interp.lastOutput
}
