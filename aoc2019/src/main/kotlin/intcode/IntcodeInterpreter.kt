package intcode

import intcode.InterpreterStatus.END_OF_PROGRAM
import intcode.InterpreterStatus.HALTED
import intcode.InterpreterStatus.INITIAL
import intcode.InterpreterStatus.RUNNING
import intcode.InterpreterStatus.WAITING_FOR_INPUT
import intcode.Operation.Add
import intcode.Operation.Halt
import intcode.Operation.Input
import intcode.Operation.Mul
import intcode.Operation.Output
import intcode.Param.AddressParam
import intcode.Param.RelativeParam
import intcode.Param.ValueParam
import utils.toLong

enum class InterpreterStatus {
    INITIAL,
    RUNNING,
    END_OF_PROGRAM,
    HALTED,
    WAITING_FOR_INPUT
}

class IntcodeInterpreter(initialMemory: List<Long>, input: List<Long> = emptyList()) {
    private var ip = 0
    private var relativeBase = 0
    private val _memory = ArrayList<Long>(initialMemory)
    private val _input: MutableList<Long> = input.toMutableList()
    private val _output: MutableList<Long> = mutableListOf()
    private var _status: InterpreterStatus = INITIAL
    private var outputCursor = 0

    val fullOutput
        get() = _output.toList()

    val lastOutput
        get() = fullOutput.last()

    val status
        get() = _status

    fun run(): InterpreterStatus {
        _status = RUNNING
        while (true) {
            val instruction = decodeInstruction()
            val result = executeInstruction(instruction)

            ip = result.newIp

            when {
                ip >= _memory.size - 1 -> {
                    _status = END_OF_PROGRAM
                    return END_OF_PROGRAM
                }
                result.shouldHalt -> {
                    _status = HALTED
                    return HALTED
                }
                result.shouldWaitForInput -> {
                    _status = WAITING_FOR_INPUT
                    return WAITING_FOR_INPUT
                }
            }
        }
    }

    private fun decodeInstruction() = Decoder(this, ip).decodeInstruction()

    data class ExecutionResult(
        val newIp: Int,
        val shouldHalt: Boolean = false,
        val shouldWaitForInput: Boolean = false
    )
    private fun executeInstruction(instruction: DecodedInstruction): ExecutionResult {
        return when (val op = instruction.operation) {
            is Add -> {
                setMemory(op.dst, resolveForRead(op.a) + resolveForRead(op.b))
                advanceIp(instruction)
            }
            is Mul -> {
                setMemory(op.dst, resolveForRead(op.a) * resolveForRead(op.b))
                advanceIp(instruction)
            }
            is Input -> {
                if (!inputAvailable()) {
                    ExecutionResult(ip, shouldWaitForInput = true)
                } else {
                    setMemory(op.dst, consumeInput())
                    advanceIp(instruction)
                }
            }
            is Output -> {
                produceOutput(resolveForRead(op.value))
                advanceIp(instruction)
            }
            is Operation.JumpIfTrue -> {
                if (resolveForRead(op.test) != 0L) {
                    setIp(resolveForRead(op.dst).toInt())
                } else {
                    advanceIp(instruction)
                }
            }
            is Operation.JumpIfFalse -> {
                if (resolveForRead(op.test) == 0L) {
                    setIp(resolveForRead(op.dst).toInt())
                } else {
                    advanceIp(instruction)
                }
            }
            is Operation.LessThan -> {
                setMemory(op.dst, (resolveForRead(op.a) < resolveForRead(op.b)).toLong())
                advanceIp(instruction)
            }
            is Operation.Equals -> {
                setMemory(op.dst, (resolveForRead(op.a) == resolveForRead(op.b)).toLong())
                advanceIp(instruction)
            }
            is Operation.AdjustRelativeBase -> {
                relativeBase += resolveForRead(op.a).toInt()
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

    private fun resolveForRead(param: Param): Long {
        return when (param) {
            is AddressParam -> getMemory(param.address)
            is ValueParam -> param.value
            is RelativeParam -> getMemory(relativeBase + param.value)
        }
    }

    private fun resolveForWrite(param: Param): Int {
        return when (param) {
            is AddressParam -> param.address
            is ValueParam -> param.value.toInt()
            is RelativeParam -> relativeBase + param.value
        }
    }

    private fun setMemory(addressParam: Param, value: Long) {
        poke(resolveForWrite(addressParam), value)
    }

    private fun padMemory(to: Int) {
        val padding = (_memory.size..to).map { 0L }
        _memory.addAll(padding)
    }

    fun poke(address: Int, value: Long) {
        if (address >= _memory.size) {
            padMemory(address)
        }

        _memory[address] = value
    }

    fun getMemory(address: Int) = _memory.getOrNull(address) ?: 0

    private fun inputAvailable() = _input.isNotEmpty()

    private fun consumeInput() = _input.removeFirst()

    fun addInput(values: List<Long>) {
        _input += values
    }

    fun addInput(value: Long) {
        _input += value
    }

    fun readOutput(): List<Long> {
        if (outputCursor == _output.size - 1) {
            return emptyList()
        }

        val result = _output.subList(outputCursor, _output.size)
        outputCursor = _output.size
        return result
    }

    private fun produceOutput(value: Long) {
        _output += value
    }

    fun clearOutput() {
        _output.clear()
    }

    fun dumpMemory(at: Int, context: Int = 5) =
        _memory
            .withIndex()
            .toList()
            .slice((at - context).coerceAtLeast(0)..(at + context).coerceAtMost(_memory.size - 1))
            .map { if (it.index == at) "**${it.value}**" else it.value.toString() }
}
