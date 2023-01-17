import utils.cartesian
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.updated

@JvmInline private value class Address(val address: Int)

private fun Int.toAddress() = Address(this)

private sealed class Opcode(val numArgs: Int) {
    val size = numArgs + 1

    data class Add(val a: Address, val b: Address, val dst: Address) : Opcode(3) {
        constructor(
            args: IntArray
        ) : this(args[0].toAddress(), args[1].toAddress(), args[2].toAddress()) {
            check(args.size == numArgs) {
                error("Too many args passed to ${this::class.simpleName}")
            }
        }
    }

    data class Mul(val a: Address, val b: Address, val dst: Address) : Opcode(3) {
        constructor(
            args: IntArray
        ) : this(args[0].toAddress(), args[1].toAddress(), args[2].toAddress()) {
            check(args.size == numArgs) {
                error("Too many args passed to ${this::class.simpleName}")
            }
        }
    }

    object Halt : Opcode(0)
}

private class IntcodeInterpreter(initialMemory: List<Int>, var ip: Int = 0) {
    private val _memory: IntArray = initialMemory.toIntArray()

    val memory
        get() = _memory.toList()

    fun run() {
        while (true) {
            val opcode = parseOpcode()
            val shouldContinue = executeOpcode(opcode)

            if (ip >= _memory.size - 1 || !shouldContinue) {
                return
            }
        }
    }

    fun executeOpcode(opcode: Opcode): Boolean {
        when (opcode) {
            is Opcode.Add -> setMemory(opcode.dst, getMemory(opcode.a) + getMemory(opcode.b))
            is Opcode.Mul -> setMemory(opcode.dst, getMemory(opcode.a) * getMemory(opcode.b))
            Opcode.Halt -> return false
        }

        ip += opcode.size
        return true
    }

    fun parseOpcode(): Opcode {
        return when (val opcode = _memory[ip]) {
            1 -> Opcode.Add(getMemorySlice(3))
            2 -> Opcode.Mul(getMemorySlice(3))
            99 -> Opcode.Halt
            else -> error("Unknown opcode: $opcode")
        }
    }

    private fun getMemory(address: Address) = _memory[address.address]
    private fun setMemory(address: Address, value: Int) {
        _memory[address.address] = value
    }
    private fun getMemorySlice(count: Int) = _memory.sliceArray(ip + 1..ip + count)
}

fun parseIntcodeProgram(raw: String) = raw.trim().split(",").map { it.toInt() }

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
