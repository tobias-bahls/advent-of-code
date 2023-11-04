package intcode

import intcode.InterpreterStatus.WAITING_FOR_INPUT

class IntcodeInteraction(private val interpreter: IntcodeInterpreter) {
    private var lastOutput: List<Long> = interpreter.readOutput()

    fun writeLine(line: String) {
        check(interpreter.status == WAITING_FOR_INPUT)

        interpreter.addInput(line + "\n")
        interpreter.run()
        lastOutput = interpreter.readOutput()
    }

    fun waitForInput() {
        interpreter.run()
        check(interpreter.status == WAITING_FOR_INPUT)
        lastOutput = interpreter.readOutput()
    }

    fun expectAsciiOutput(expected: String) {
        check(lastOutput.asString() == expected) {
            "Expected ASCII output <$expected>, but got <$lastOutput>"
        }
    }

    fun expectOutput(msg: String, block: (List<Long>) -> Boolean) {
        check(block(lastOutput)) { "$msg, actual output: <${lastOutput.asString()}>" }
    }
}

fun IntcodeInterpreter.interact(block: IntcodeInteraction.() -> Unit) {
    val interaction = IntcodeInteraction(this)
    block(interaction)
}
