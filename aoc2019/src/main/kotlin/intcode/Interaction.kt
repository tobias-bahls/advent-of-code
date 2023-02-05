package intcode

import intcode.InterpreterStatus.WAITING_FOR_INPUT

class IntcodeInteraction(private val interpreter: IntcodeInterpreter) {
    private var lastOutput: List<Long> = emptyList()

    private val lastOutputAscii
        get() = lastOutput.map { it.toInt().toChar() }.joinToString("")

    fun writeLine(line: String) {
        check(interpreter.status == WAITING_FOR_INPUT)

        interpreter.addInput((line + "\n").map { it.code.toLong() })
        interpreter.run()
        lastOutput = interpreter.readOutput()
    }

    fun expectAsciiOutput(expected: String) {
        check(lastOutputAscii == expected) {
            "Expected ASCII output <$expected>, but got <$lastOutputAscii>"
        }
    }
}

fun IntcodeInterpreter.interact(block: IntcodeInteraction.() -> Unit) {
    val interaction = IntcodeInteraction(this)
    block(interaction)
}
