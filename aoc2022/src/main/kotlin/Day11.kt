import utils.Part
import utils.filterNotBlank
import utils.lcm
import utils.match
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.singleMatch
import utils.twoParts

fun parseOperation(operation: String): (Long) -> Long {
    val (op, rhs) = operation.match("""old (\+|\*) (\d+|old)""")

    val rhsInt = rhs.toIntOrNull()?.toLong()

    return when (op) {
        "+" ->
            if (rhsInt == null) {
                return { old -> old + old }
            } else {
                return { old -> old + rhsInt }
            }
        "*" ->
            if (rhsInt == null) {
                return { old -> old * old }
            } else {
                return { old -> old * rhsInt }
            }
        else -> error("Unknown operator: $op")
    }
}

data class Throw(val item: Long, val target: Int)

data class Monkey(
    val id: Int,
    var items: MutableList<Long>,
    val operation: (Long) -> Long,
    val test: Int,
    val trueTarget: Int,
    val falseTarget: Int
) {

    var inspectionCount: Long = 0.toLong()

    fun turn(part: Part): List<Throw> {
        return items
            .map {
                val newWorryLevel =
                    if (part == Part.PART1) {
                        operation(it) / 3.toLong()
                    } else {
                        operation(it)
                    }

                if (newWorryLevel % test.toLong() == 0.toLong()) {
                    Throw(newWorryLevel, trueTarget)
                } else {
                    Throw(newWorryLevel, falseTarget)
                }
            }
            .also {
                inspectionCount += items.size.toLong()
                items.clear()
            }
    }

    fun receiveItem(item: Long) {
        this.items += item
    }

    fun moduloItems(by: Long) {
        this.items = this.items.map { it % by }.toMutableList()
    }
}

fun parseMonkey(input: String): Monkey {
    val lines = input.lines()

    return Monkey(
        id = lines[0].match("""Monkey (\d+):""").singleMatch().toInt(),
        items =
            lines[1]
                .twoParts(":")
                .second
                .split(",")
                .filterNotBlank()
                .map { it.toLong() }
                .toMutableList(),
        operation = lines[2].twoParts(" = ").second.trim().let { parseOperation(it) },
        test = lines[3].twoParts("divisible by ").second.trim().toInt(),
        trueTarget = lines[4].twoParts("monkey ").second.trim().toInt(),
        falseTarget = lines[5].twoParts("monkey ").second.trim().toInt())
}

fun round(monkeys: List<Monkey>, part: Part) {
    monkeys.forEach { monkey ->
        monkey.turn(part).forEach { t ->
            val receiver = monkeys.find { it.id == t.target } ?: error("Invalid throw: $t")
            receiver.receiveItem(t.item)
        }
    }
}

fun roundPart2(monkeys: List<Monkey>) {
    round(monkeys, Part.PART2)

    val allItemsLCM = monkeys.map { it.test.toLong() }.reduce(Long::lcm)
    monkeys.forEach { it.moduloItems(allItemsLCM) }
}

private fun solve(monkeys: List<Monkey>): Long {
    val (first, second) = monkeys.map { it.inspectionCount }.sortedDescending()

    return first * second
}

fun main() {
    val input = readResourceAsString("/day11.txt")

    part1 {
        val monkeys = input.split("\n\n").map { parseMonkey(it) }
        repeat(20) { round(monkeys, Part.PART1) }
        solve(monkeys)
    }

    part2 {
        val monkeys = input.split("\n\n").map { parseMonkey(it) }
        repeat(10000) { roundPart2(monkeys) }
        solve(monkeys)
    }
}
