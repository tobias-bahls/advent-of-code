package intcode

import intcode.IntcodeInterpreter.ParameterMode.*
import intcode.Opcode.Add
import intcode.Opcode.Halt
import intcode.Opcode.Mul
import intcode.OpcodeParameter.AddressParameter
import intcode.OpcodeParameter.ValueParameter

class IntcodeInterpreter(initialMemory: List<Int>, var ip: Int = 0) {
    private val _memory: IntArray = initialMemory.toIntArray()

    val memory
        get() = _memory.toList()

    fun run() {
        while (true) {
            val opcode = decodeInstructionToOpcode()
            val shouldContinue = executeOpcode(opcode)

            if (ip >= _memory.size - 1 || !shouldContinue) {
                return
            }
        }
    }

    private fun executeOpcode(opcode: Opcode): Boolean {
        when (opcode) {
            is Add -> setMemory(opcode.dst, resolve(opcode.a) + resolve(opcode.b))
            is Mul -> setMemory(opcode.dst, resolve(opcode.a) * resolve(opcode.b))
            Halt -> return false
        }
        return true
    }

    private fun resolve(opcodeParameter: OpcodeParameter): Int {
        return when (opcodeParameter) {
            is AddressParameter -> _memory[opcodeParameter.address]
            is ValueParameter -> opcodeParameter.value
        }
    }

    private fun decodeInstructionToOpcode(): Opcode {
        val decoded = decodeInstruction(_memory[ip++])
        return when (val opcode = decoded.opcode) {
            1 -> Add(param(decoded.mode(0)), param(decoded.mode(1)), positionParam())
            2 -> Mul(param(decoded.mode(0)), param(decoded.mode(1)), positionParam())
            99 -> Halt
            else -> error("Unknown opcode: $opcode at ${ip-1}: ${dumpMemory(ip-1)}")
        }
    }

    private fun param(mode: ParameterMode): OpcodeParameter {
        return when (mode) {
            POSITION -> positionParam()
            IMMEDIATE -> immediateParam()
        }
    }

    private fun positionParam() = AddressParameter(_memory[ip++])
    private fun immediateParam() = ValueParameter(_memory[ip++])

    enum class ParameterMode {
        POSITION,
        IMMEDIATE,
    }
    data class DecodedInstruction(val opcode: Int, val parameterModes: List<ParameterMode>) {
        fun mode(index: Int) = parameterModes.getOrNull(index) ?: POSITION
    }
    fun decodeInstruction(instruction: Int): DecodedInstruction {
        val opcode = (instruction / 10) % 10 * 10 + instruction % 10

        val parameterModes = mutableListOf<ParameterMode>()
        var remaining = instruction / 100
        while (remaining > 0) {
            parameterModes +=
                (remaining % 10).let {
                    when (it) {
                        0 -> POSITION
                        1 -> IMMEDIATE
                        else -> error("Unknown parameter mode: $it")
                    }
                }

            remaining /= 10
        }

        return DecodedInstruction(opcode, parameterModes)
    }

    private fun setMemory(address: AddressParameter, value: Int) {
        _memory[address.address] = value
    }

    private fun dumpMemory(at: Int, context: Int = 5) =
        _memory.withIndex().toList().slice(at - context..at + context).map {
            if (it.index == at) "**${it.value}**" else it.value.toString()
        }
}
