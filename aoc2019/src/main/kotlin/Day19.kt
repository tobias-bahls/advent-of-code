import intcode.IntcodeInterpreter
import intcode.parseIntcodeProgram
import utils.part1
import utils.part2
import utils.readResourceAsString

private fun isPulled(program: List<Long>, x: Int, y: Int): Boolean {
    val intcodeInterpreter = IntcodeInterpreter(program)
    intcodeInterpreter.addInput(x.toLong())
    intcodeInterpreter.addInput(y.toLong())
    intcodeInterpreter.run()
    return intcodeInterpreter.lastOutput == 1L
}

fun main() {
    val program = parseIntcodeProgram(readResourceAsString("/day19.txt"))

    part1 { (0 until 50).sumOf { y -> (0 until 50).count { x -> isPulled(program, x, y) } } }

    part2 {
        var minX: Int
        var x = 0
        var y = 4

        while (true) {
            while (!isPulled(program, x, y)) {
                x++
            }

            minX = x
            while (isPulled(program, x, y)) {
                x++

                if (isPulled(program, x + 99, y + 99) &&
                    isPulled(program, x, y + 99) &&
                    isPulled(program, x + 99, y)) {
                    return@part2 (x * 10000) + y
                }
            }
            y++
            x = minX
        }
    }
}
