import utils.map
import utils.match
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.setBit
import utils.toPair

private data class Bitmask(val raw: String) {
    private val orMask = raw.replace("X", "0").toLong(2)
    private val andMask = raw.replace("X", "1").toLong(2)
    private val floatingBits =
        raw.reversed().withIndex().filter { it.value == 'X' }.map { it.index }

    fun apply(to: Long) = (to and andMask) or orMask

    fun applyToMemory(to: Long): List<Long> {
        val orMaskApplied = to or orMask

        fun applyAllFloatingBits(remainingBits: List<Int>, current: Long): List<Long> {
            if (remainingBits.isEmpty()) {
                return listOf(current)
            }

            val bitToSet = remainingBits.first()
            val zeroSet = current.setBit(bitToSet, false)
            val oneSet = current.setBit(bitToSet, true)

            val newRemaining = remainingBits.drop(1)
            return applyAllFloatingBits(newRemaining, zeroSet) +
                applyAllFloatingBits(newRemaining, oneSet)
        }

        return applyAllFloatingBits(floatingBits, orMaskApplied)
    }
}

private data class DockingProcessor(
    val memory: Map<Long, Long> = mapOf(),
    val bitmask: Bitmask? = null
) {
    fun setMemoryPart1(index: Long, value: Long): DockingProcessor {
        checkNotNull(bitmask)
        return copy(memory = memory + (index to bitmask.apply(value)))
    }

    fun setMemoryPart2(index: Long, value: Long): DockingProcessor {
        checkNotNull(bitmask)
        val memoryToSet = bitmask.applyToMemory(index).map { it to value }

        return copy(memory = memory + memoryToSet)
    }

    fun setBitmask(newBitmask: Bitmask) = copy(bitmask = newBitmask)
}

private sealed interface DockingProcessorInstruction {
    data class SetBitmask(val newMask: Bitmask) : DockingProcessorInstruction
    data class SetMemory(val index: Long, val to: Long) : DockingProcessorInstruction
}

private fun parseDockingProcessorInstruction(input: String): DockingProcessorInstruction {
    return if (input.startsWith("mask")) {
        val bitmask = input.replace("mask = ", "")
        DockingProcessorInstruction.SetBitmask(Bitmask(bitmask))
    } else {
        val (index, value) = input.match("""mem\[(\d+)\] = (\d+)""").toPair().map { it.toLong() }

        DockingProcessorInstruction.SetMemory(index, value)
    }
}

fun main() {
    val input = readResourceAsString("/day14.txt")
    val instructions = input.parseLines { parseDockingProcessorInstruction(it) }

    fun solve(
        instructions: List<DockingProcessorInstruction>,
        memorySettingFunction: (DockingProcessor, Long, Long) -> DockingProcessor
    ): Long {
        val resultProcessor =
            instructions.fold(DockingProcessor()) { processor, ins ->
                when (ins) {
                    is DockingProcessorInstruction.SetBitmask -> processor.setBitmask(ins.newMask)
                    is DockingProcessorInstruction.SetMemory ->
                        memorySettingFunction(processor, ins.index, ins.to)
                }
            }

        return resultProcessor.memory.values.sum()
    }

    part1 { solve(instructions, DockingProcessor::setMemoryPart1) }
    part2 { solve(instructions, DockingProcessor::setMemoryPart2) }
}
