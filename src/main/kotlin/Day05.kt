import Part.PART1
import Part.PART2

@JvmInline value class Crate(val char: Char)

data class Crates(val stacks: Map<Int, List<Crate>>) {
    companion object {
        fun parse(input: String): Crates {
            val lines = input.lines()
            val stackIndexes = lines.last().split(" ").filterNotBlank().mapInts()

            val stacks =
                stackIndexes.associateWith { i ->
                    val column = ((i - 1) * 4) + 1

                    val stack =
                        lines
                            .dropLast(1)
                            .map { it.getOrNull(column) ?: ' ' }
                            .filterWhitespace()
                            .map { Crate(it) }

                    stack.reversed().toMutableList()
                }

            return Crates(stacks)
        }
    }

    fun executeInstruction(instruction: Instruction, mode: Part): Crates {
        val fromStack = stacks.getValue(instruction.from)
        val toStack = stacks.getValue(instruction.to)

        val moved =
            when (mode) {
                PART1 -> fromStack.reversed().take(instruction.count)
                PART2 -> fromStack.takeLast(instruction.count)
            }

        val newTo = toStack + moved
        val newFrom = fromStack.dropLast(instruction.count)

        val newStacks =
            stacks.mapValues { (idx, stack) ->
                when (idx) {
                    instruction.from -> newFrom
                    instruction.to -> newTo
                    else -> stack
                }
            }

        return Crates(newStacks)
    }
}

data class Instruction(val count: Int, val from: Int, val to: Int) {
    companion object {

        fun parse(input: String): Instruction {
            val (count, from, to) = input.match("""move (\d+) from (\d+) to (\d+)""")

            return Instruction(count.toInt(), from.toInt(), to.toInt())
        }
    }
}

fun main() {
    val input = readResourceAsString("day05.txt")

    val (rawStack, rawInstructions) = input.twoParts("\n\n").toPair()
    val crates = Crates.parse(rawStack)
    val instructions = rawInstructions.parseLines { Instruction.parse(it) }

    part1 {
        instructions
            .fold(crates) { acc, instruction -> acc.executeInstruction(instruction, PART1) }
            .stacks
            .values
            .map { it.last().char }
            .joinToString("")
    }

    part2 {
        instructions
            .fold(crates) { acc, instruction -> acc.executeInstruction(instruction, PART2) }
            .stacks
            .values
            .map { it.last().char }
            .joinToString("")
    }
}
