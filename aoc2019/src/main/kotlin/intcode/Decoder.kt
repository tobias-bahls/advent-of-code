package intcode

import intcode.Decoder.ParameterMode.IMMEDIATE
import intcode.Decoder.ParameterMode.POSITION
import intcode.Decoder.ParameterMode.RELATIVE
import intcode.Operation.*
import intcode.Param.AddressParam
import intcode.Param.RelativeParam
import intcode.Param.ValueParam
import kotlin.properties.Delegates

data class DecodedInstruction(val operation: Operation, val size: Int)

class Decoder(private val interpreter: IntcodeInterpreter, private val initialPosition: Int) {
    private var offset: Int = 0
    private var paramIndex: Int = 0
    private var opcode by Delegates.notNull<Long>()
    private lateinit var parameterModes: List<ParameterMode>

    fun decodeInstruction(): DecodedInstruction {
        decodeOpcodeAndParameterModes()

        val operation =
            when (opcode) {
                1L -> Add(param(), param(), param())
                2L -> Mul(param(), param(), param())
                3L -> Input(param())
                4L -> Output(param())
                5L -> JumpIfTrue(param(), param())
                6L -> JumpIfFalse(param(), param())
                7L -> LessThan(param(), param(), param())
                8L -> Equals(param(), param(), param())
                9L -> AdjustRelativeBase(param())
                99L -> Halt
                else ->
                    error(
                        "Unknown opcode: $opcode at ${initialPosition}: ${interpreter.dumpMemory(initialPosition)}")
            }

        return DecodedInstruction(operation, offset)
    }

    enum class ParameterMode {
        POSITION,
        IMMEDIATE,
        RELATIVE
    }

    private fun param(): Param {
        return when (parameterModes.getOrNull(paramIndex++)) {
            POSITION -> AddressParam(interpreter.getMemory(initialPosition + offset++).toInt())
            IMMEDIATE -> ValueParam(interpreter.getMemory(initialPosition + offset++))
            RELATIVE -> RelativeParam(interpreter.getMemory(initialPosition + offset++).toInt())
            null -> AddressParam(interpreter.getMemory(initialPosition + offset++).toInt())
        }
    }

    private fun decodeOpcodeAndParameterModes() {
        val instruction = interpreter.getMemory(initialPosition + offset++)

        this.opcode = (instruction / 10) % 10 * 10 + instruction % 10

        val parameterModes = mutableListOf<ParameterMode>()
        var remaining = instruction / 100
        while (remaining > 0) {
            parameterModes +=
                (remaining % 10).let {
                    when (it) {
                        0L -> POSITION
                        1L -> IMMEDIATE
                        2L -> RELATIVE
                        else ->
                            error(
                                "Unknown parameter mode: $it when parsing [$instruction] at $initialPosition")
                    }
                }

            remaining /= 10
        }

        this.parameterModes = parameterModes
    }
}
