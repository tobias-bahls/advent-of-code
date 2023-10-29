import Instruction.FoldX
import Instruction.FoldY
import datastructures.Point2D
import datastructures.SparseGrid
import datastructures.parsePoint2D
import kotlin.math.absoluteValue
import utils.map
import utils.mapSecond
import utils.match
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toPair
import utils.twoParts
import utils.wrap

sealed interface Instruction {
    data class FoldX(val at: Int) : Instruction

    data class FoldY(val at: Int) : Instruction
}

fun parseInstruction(input: String): Instruction {
    val (axis, at) = input.match("""fold along (.)=(\d+)""").toPair().mapSecond { it.toInt() }

    return when (axis) {
        "x" -> FoldX(at)
        "y" -> FoldY(at)
        else -> error("Unknown axis: $axis")
    }
}

fun applyInstruction(points: Set<Point2D>, instruction: Instruction): Set<Point2D> {
    return when (instruction) {
        is FoldX -> foldAlongX(points, instruction.at)
        is FoldY -> foldAlongY(points, instruction.at)
    }
}

fun foldAlongY(points: Set<Point2D>, at: Int): Set<Point2D> {
    val (toFold, remaining) = points.partition { it.y > at }.map { it.toSet() }

    val folded = toFold.map { Point2D(it.x, (it.y - at * 2).absoluteValue) }.toSet()

    return remaining + folded
}

fun foldAlongX(points: Set<Point2D>, at: Int): Set<Point2D> {
    val (toFold, remaining) = points.partition { it.x > at }.map { it.toSet() }

    val folded = toFold.map { Point2D((it.x - at * 2).absoluteValue, it.y) }.toSet()

    return remaining + folded
}

fun main() {
    val input = readResourceAsString("/day13.txt")

    val (gridInput, instructionsInput) = input.twoParts("\n\n")

    val points = gridInput.parseLines { parsePoint2D(it) }.toSet()
    val instructions = instructionsInput.parseLines { parseInstruction(it) }

    part1 {
        val instruction = instructions.first()

        applyInstruction(points, instruction).size
    }

    part2 {
        val applied = instructions.fold(points) { acc, it -> applyInstruction(acc, it) }

        val grid = SparseGrid(applied)

        (0..grid.height)
            .joinToString("\n") { y ->
                (0..grid.width).joinToString("") { x ->
                    if (grid.hasPoint(x, y)) {
                        "█"
                    } else {
                        " "
                    }
                }
            }
            .wrap("\n")
    }
}
