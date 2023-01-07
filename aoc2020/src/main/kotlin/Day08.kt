import utils.match
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.updated

private sealed interface Instruction {
    data class Nop(val by: Int) : Instruction
    data class Acc(val by: Int) : Instruction
    data class Jmp(val by: Int) : Instruction
}

private fun parseInstruction(input: String): Instruction {
    val (ins, arg) = input.match("""(nop|acc|jmp) ([+-][0-9]+)""")

    return when (ins) {
        "nop" -> Instruction.Nop(arg.toInt())
        "acc" -> Instruction.Acc(arg.toInt())
        "jmp" -> Instruction.Jmp(arg.toInt())
        else -> error("Unknown instruction: $ins")
    }
}

private data class ConsoleCPU(
    val program: List<Instruction>,
    val accumulator: Int = 0,
    val pc: Int = 0,
    val seen: Set<Int> = emptySet()
) {
    val terminated = pc == program.size
    val looped = pc in seen

    fun step() =
        when (val ins = program[pc]) {
            is Instruction.Nop -> copy(pc = pc + 1, seen = seen + pc)
            is Instruction.Acc ->
                copy(pc = pc + 1, seen = seen + pc, accumulator = accumulator + ins.by)
            is Instruction.Jmp -> copy(pc = pc + ins.by, seen = seen + pc)
        }
}

private fun runUntilTerminationOrLoop(program: List<Instruction>): ConsoleCPU {
    var current = ConsoleCPU(program)
    while (!current.terminated && !current.looped) {
        current = current.step()
    }

    return current
}

fun main() {
    val input = readResourceAsString("/day08.txt")
    val program = input.parseLines { parseInstruction(it) }

    part1 {
        val result = runUntilTerminationOrLoop(program)

        check(result.looped) { "Should have found loop" }
        result.accumulator
    }

    part2 {
        program
            .mapIndexed { index, ins ->
                when (ins) {
                    is Instruction.Acc -> program
                    is Instruction.Jmp -> program.updated(index, Instruction.Nop(ins.by))
                    is Instruction.Nop -> program.updated(index, Instruction.Jmp(ins.by))
                }
            }
            .asSequence()
            .map { runUntilTerminationOrLoop(it) }
            .find { it.terminated }
            ?.accumulator
            ?: error("Could not find program that terminates")
    }
}
