import intcode.IntcodeInterpreter
import intcode.InterpreterStatus.HALTED
import intcode.InterpreterStatus.WAITING_FOR_INPUT
import intcode.asString
import intcode.parseIntcodeProgram
import utils.readResourceAsString

fun main() {
    val input = readResourceAsString("/day25.txt")
    val program = parseIntcodeProgram(input)

    val computer = IntcodeInterpreter(program)

    while (true) {
        computer.run()

        val output = computer.readOutput().asString()
        if (output.isNotEmpty()) {
            println(output)
        }
        if (computer.status == WAITING_FOR_INPUT) {
            print("> ")
            computer.addInput(readCommand())
        }
        if (computer.status == HALTED) {
            break
        }
    }
}

private fun readCommand() =
    when (val read = readln()) {
        "n" -> "north"
        "e" -> "east"
        "s" -> "south"
        "w" -> "west"
        else -> read
    } + "\n"
