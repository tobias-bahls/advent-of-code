package intcode

import intcode.Decoder.ParameterMode.IMMEDIATE
import intcode.Decoder.ParameterMode.POSITION
import intcode.Operation.*
import intcode.Param.AddressParam
import intcode.Param.ValueParam
import kotlin.properties.Delegates

data class DecodedInstruction(val operation: Operation, val size: Int)

class Decoder(private val interpreter: IntcodeInterpreter, private val initialPosition: Int) {
    private val memory
        get() = interpreter.memory

    private var offset: Int = 0
    private var paramIndex: Int = 0
    private var opcode by Delegates.notNull<Int>()
    private lateinit var parameterModes: List<ParameterMode>

    fun decodeInstruction(): DecodedInstruction {
        decodeOpcodeAndParameterModes()

        val operation =
            when (opcode) {
                1 -> Add(dynamicParam(), dynamicParam(), addressParam())
                2 -> Mul(dynamicParam(), dynamicParam(), addressParam())
                3 -> Input(addressParam())
                4 -> Output(dynamicParam())
                5 -> JumpIfTrue(dynamicParam(), dynamicParam())
                6 -> JumpIfFalse(dynamicParam(), dynamicParam())
                7 -> LessThan(dynamicParam(), dynamicParam(), addressParam())
                8 -> Equals(dynamicParam(), dynamicParam(), addressParam())
                99 -> Halt
                else ->
                    error(
                        "Unknown opcode: $opcode at ${initialPosition}: ${interpreter.dumpMemory(initialPosition)}")
            }

        return DecodedInstruction(operation, offset)
    }

    enum class ParameterMode {
        POSITION,
        IMMEDIATE,
    }

    private fun dynamicParam(): Param {
        return when (parameterModes.getOrNull(paramIndex++)) {
            POSITION -> addressParam()
            IMMEDIATE -> valueParam()
            null -> addressParam()
        }
    }

    private fun addressParam() = AddressParam(memory[initialPosition + offset++])
    private fun valueParam() = ValueParam(memory[initialPosition + offset++])

    private fun decodeOpcodeAndParameterModes() {
        val instruction = memory[initialPosition + offset++]
        this.opcode = (instruction / 10) % 10 * 10 + instruction % 10

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

        this.parameterModes = parameterModes
    }
}
