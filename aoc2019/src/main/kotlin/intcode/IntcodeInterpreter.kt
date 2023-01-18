package intcode

class IntcodeInterpreter(initialMemory: List<Int>, var ip: Int = 0) {
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
