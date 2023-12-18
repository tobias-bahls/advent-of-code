import datastructures.*
import datastructures.CardinalDirection.*
import utils.*

private data class Day18Instruction(
    val dir: CardinalDirectionOrthogonal,
    val count: Int,
    val color: String
) {
    fun decode(): Day18Instruction =
        Day18Instruction(
            dir =
                when (this.color.last()) {
                    '0' -> East
                    '1' -> South
                    '2' -> West
                    '3' -> North
                    else -> unreachable("can't decode $this")
                },
            count = this.color.substring(1, 6).toInt(16),
            color = this.color)
}

private fun parseInstruction(str: String) =
    str.match("([LRUD]) (\\d+) \\((.*)\\)").let { (dir, count, color) ->
        Day18Instruction(
            dir =
                when (dir) {
                    "L" -> West
                    "R" -> East
                    "U" -> North
                    "D" -> South
                    else -> unreachable("unknown dir: $dir")
                },
            count = count.toInt(),
            color = color)
    }

private fun solve(instructions: List<Day18Instruction>): Long {
    val points =
        instructions.fold(listOf(Point2D.ZERO)) { points, ins ->
            val next = points.last() + (ins.dir.movementVector * ins.count)

            points + next
        }

    val shoelaceArea =
        points.windowed(2, 1).sumOf { (point, next) ->
            (point.x.toLong() * next.y) - (next.x.toLong() * point.y)
        } / 2

    val trenchLengths = instructions.sumOf { it.count.toLong() } / 2

    return shoelaceArea + trenchLengths + 1
}

fun main() {
    part1 {
        val input = readResourceAsString("/day18.txt")

        val instructions = input.parseLines { parseInstruction(it) }
        solve(instructions)
    }

    part2 {
        val input = readResourceAsString("/day18.txt")

        val instructions = input.parseLines { parseInstruction(it) }.map { it.decode() }

        solve(instructions)
    }
}
