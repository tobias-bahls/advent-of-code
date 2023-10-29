import utils.BLOCK
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.twoParts

sealed class Day10Instruction(val cycles: Int) {
    object Noop : Day10Instruction(cycles = 1)

    data class AddX(val v: Int) : Day10Instruction(cycles = 2)
}

fun parseDay10Instruction(input: String): Day10Instruction =
    when {
        input == "noop" -> Day10Instruction.Noop
        input.startsWith("addx") ->
            input.twoParts(" ").second.let { Day10Instruction.AddX(it.toInt()) }
        else -> error("Cant parse instruction $input")
    }

class CPU(private val program: List<Day10Instruction>) {
    var x = 1
    var cycles = 0

    private var pc = 0
    private var currentInstruction: Day10Instruction = program[0]
    private var instructionRunLength = 0

    fun cycle(times: Int = 1): CPU {
        repeat(times) { this.cycle() }
        return this
    }

    fun drawsPixel(screenX: Int): Boolean {
        val spriteRange = (screenX - 1).rangeTo(screenX + 1)

        return x in spriteRange
    }

    private fun cycle(): CPU {
        if (instructionRunLength == currentInstruction.cycles) {
            executeInstruction(currentInstruction)
            pc = (pc + 1) % program.size
            currentInstruction = program[pc]
            instructionRunLength = 1
        } else {
            instructionRunLength++
        }

        cycles++
        return this
    }

    private fun executeInstruction(ins: Day10Instruction) {
        when (ins) {
            is Day10Instruction.Noop -> Unit
            is Day10Instruction.AddX -> this.x += ins.v
        }
    }
}

fun main() {

    val input = readResourceAsString("/day10.txt")
    val program = input.parseLines { parseDay10Instruction(it) }

    part1 {
        val cpu = CPU(program)
        listOf(20, 60, 100, 140, 180, 220).sumOf { it * cpu.cycle(it - cpu.cycles).x }
    }

    part2 {
        val cpu = CPU(program)

        val map =
            0.until(40).map { x ->
                if (cpu.cycle().drawsPixel(x)) {
                    BLOCK
                } else {
                    " "
                }
            }
        "\n" + 0.until(6).flatMap { _ -> map + "\n" }.joinToString("")
    }
}
