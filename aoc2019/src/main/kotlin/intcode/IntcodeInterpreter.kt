package intcode

import intcode.Operation.Add
import intcode.Operation.Halt
import intcode.Operation.Input
import intcode.Operation.Mul
import intcode.Operation.Output
import intcode.Param.AddressParam
import intcode.Param.ValueParam
import utils.toInt

class IntcodeInterpreter(initialMemory: List<Int>, input: List<Int> = emptyList()) {
    private var ip = 0
    private val _memory: IntArray = initialMemory.toIntArray()
    private val _input: MutableList<Int> = input.toMutableList()
    private val _output: MutableList<Int> = mutableListOf()

    val memory
        get() = _memory.toList()

    val output
        get() = _output.toList()

    fun run() {
        while (true) {
            val instruction = decodeInstruction()
            val result = executeInstruction(instruction)

            ip = result.newIp

            if (ip >= _memory.size - 1 || result.shouldHalt) {
                return
            }
        }
    }

    private fun decodeInstruction() = Decoder(this, ip).decodeInstruction()

    data class ExecutionResult(val newIp: Int, val shouldHalt: Boolean = false)
    private fun executeInstruction(instruction: DecodedInstruction): ExecutionResult {
        return when (val op = instruction.operation) {
            is Add -> {
                setMemory(op.dst, resolve(op.a) + resolve(op.b))
                advanceIp(instruction)
            }
            is Mul -> {
                setMemory(op.dst, resolve(op.a) * resolve(op.b))
                advanceIp(instruction)
            }
            is Input -> {
                setMemory(op.dst, consumeInput())
                advanceIp(instruction)
            }
            is Output -> {
                produceOutput(resolve(op.value))
                advanceIp(instruction)
            }
            is Operation.JumpIfTrue -> {
                if (resolve(op.test) != 0) {
                    setIp(resolve(op.dst))
                } else {
                    advanceIp(instruction)
                }
            }
            is Operation.JumpIfFalse -> {
                if (resolve(op.test) == 0) {
                    setIp(resolve(op.dst))
                } else {
                    advanceIp(instruction)
                }
            }
            is Operation.LessThan -> {
                setMemory(op.dst, (resolve(op.a) < resolve(op.b)).toInt())
                advanceIp(instruction)
            }
            is Operation.Equals -> {
                setMemory(op.dst, (resolve(op.a) == resolve(op.b)).toInt())
                advanceIp(instruction)
            }
            is Halt -> halt(instruction)
        }
    }

    private fun setIp(newIp: Int): ExecutionResult = ExecutionResult(newIp, false)

    private fun advanceIp(instruction: DecodedInstruction): ExecutionResult =
        ExecutionResult(ip + instruction.size, false)
    private fun halt(instruction: DecodedInstruction): ExecutionResult =
        advanceIp(instruction).copy(shouldHalt = true)

    private fun resolve(param: Param): Int {
        return when (param) {
            is AddressParam -> _memory[param.address]
            is ValueParam -> param.value
        }
    }

    private fun setMemory(address: AddressParam, value: Int) {
        _memory[address.address] = value
    }

    private fun consumeInput() = _input.removeFirst()
    private fun produceOutput(value: Int) {
        _output += value
    }

    fun dumpMemory(at: Int, context: Int = 5) =
        _memory
            .withIndex()
            .toList()
            .slice((at - context).coerceAtLeast(0)..(at + context).coerceAtMost(_memory.size - 1))
            .map { if (it.index == at) "**${it.value}**" else it.value.toString() }
}
