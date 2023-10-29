import Direction.DOWN
import Direction.LEFT
import Direction.RIGHT
import Direction.UP
import datastructures.Point2D
import utils.clamp
import utils.mapFirst
import utils.mapSecond
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.reduceTimes
import utils.repeatedList
import utils.twoParts

enum class Direction {
    UP,
    RIGHT,
    DOWN,
    LEFT
}

fun parseDirection(input: String) =
    when (input) {
        "U" -> UP
        "R" -> RIGHT
        "D" -> DOWN
        "L" -> LEFT
        else -> error("Can't parse direction: $input")
    }

data class Day09Instruction(val direction: Direction, val steps: Int)

fun parseInstruction(input: String) =
    input
        .twoParts(" ")
        .mapFirst { parseDirection(it) }
        .mapSecond { it.toInt() }
        .let { (dir, steps) -> Day09Instruction(dir, steps) }

data class Rope(val segments: List<Point2D>) {
    val head
        get() = segments.first()

    val last
        get() = segments.last()

    val tail
        get() = segments.drop(1)
}

private fun moveHead(instruction: Day09Instruction, head: Point2D): Point2D =
    when (instruction.direction) {
        UP -> head.top
        RIGHT -> head.right
        DOWN -> head.bottom
        LEFT -> head.left
    }

fun moveTail(tail: Point2D, newHead: Point2D): Point2D {
    val distance = tail.distanceTo(newHead)
    if (distance < 2) {
        return tail
    }

    val delta = tail.minus(newHead)
    return when {
        delta.y == 0 && delta.x > 0 -> tail + Point2D(-1, 0)
        delta.y == 0 && delta.x < 0 -> tail + Point2D(1, 0)
        delta.x == 0 && delta.y > 0 -> tail + Point2D(0, -1)
        delta.x == 0 && delta.y < 0 -> tail + Point2D(0, 1)
        else -> tail - Point2D(delta.x.clamp(-1, 1), delta.y.clamp(-1, 1))
    }
}

fun applyInstruction(initialRope: Rope, instruction: Day09Instruction): List<Rope> {
    return reduceTimes(instruction.steps, listOf(initialRope)) { ropes ->
        val rope = ropes.last()

        val newHead = moveHead(instruction, rope.head)

        var previous = newHead
        val newTail =
            rope.tail.map {
                previous = moveTail(it, previous)
                previous
            }

        ropes + Rope(listOf(newHead) + newTail)
    }
}

fun applyAllInstructions(instructions: List<Day09Instruction>, initialRope: Rope): List<Rope> {
    return instructions.fold(listOf(initialRope)) { currentPath, ins ->
        val path = applyInstruction(currentPath.last(), ins)

        currentPath + path
    }
}

fun main() {
    val input = readResourceAsString("/day09.txt")
    val instructions = input.parseLines { parseInstruction(it) }

    part1 {
        val initialRope = Rope(repeatedList(2) { Point2D(0, 0) })
        applyAllInstructions(instructions, initialRope).map { it.last }.distinct().size
    }

    part2 {
        val initialRope = Rope(repeatedList(10) { Point2D(0, 0) })
        applyAllInstructions(instructions, initialRope).map { it.last }.distinct().size
    }
}
