import intcode.IntcodeInterpreter
import intcode.InterpreterStatus.HALTED
import intcode.parseIntcodeProgram
import utils.part1
import utils.part2
import utils.permutations
import utils.readResourceAsString

private fun calculateThrusterOutputPart1(phaseSettings: List<Int>, program: List<Int>): Int {
    return phaseSettings.fold(0) { currentOutput, currentPhaseSetting ->
        val interpreter = IntcodeInterpreter(program, listOf(currentPhaseSetting, currentOutput))
        interpreter.run()

        interpreter.output.first()
    }
}

private fun calculateThrusterOutputPart2(phaseSettings: List<Int>, program: List<Int>): Int {
    val computers = phaseSettings.map { IntcodeInterpreter(program, listOf(it)) }
    computers.forEach { it.run() }

    val aThruster = computers.first()
    val eThruster = computers.last()

    aThruster.addInput(0)
    aThruster.run()
    while (true) {
        computers.zipWithNext { a, b ->
            b.addInput(a.lastOutput)
            b.run()
        }
        if (computers.any { it.status == HALTED }) {
            break
        }

        aThruster.addInput(eThruster.lastOutput)
        aThruster.run()
    }

    return eThruster.lastOutput
}

fun main() {
    val input = readResourceAsString("/day07.txt")
    val program = parseIntcodeProgram(input)

    part1 { (0..4).permutations().maxOf { calculateThrusterOutputPart1(it, program) } }
    part2 { (5..9).permutations().maxOf { calculateThrusterOutputPart2(it, program) } }
}
