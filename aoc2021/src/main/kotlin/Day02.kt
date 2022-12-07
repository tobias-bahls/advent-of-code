import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString

enum class Direction {
    Forward,
    Up,
    Down
}

data class Command(val direction: Direction, val units: Int)

fun parseLine(it: String): Command {
    val split = it.split(" ")

    return Command(
        direction =
            when (split.first().lowercase()) {
                "forward" -> Direction.Forward
                "up" -> Direction.Up
                "down" -> Direction.Down
                else -> error("Invalid direction: [$it]")
            },
        units = Integer.parseInt(split.last()))
}

data class Location(val horizontal: Int = 0, val vertical: Int = 0, val aim: Int = 0)

fun main() {
    val input = readResourceAsString("/day01.txt").parseLines { parseLine(it) }

    part1 {
        val part1 =
            input.fold(Location()) { loc, cmd ->
                when (cmd.direction) {
                    Direction.Forward -> loc.copy(horizontal = loc.horizontal + cmd.units)
                    Direction.Up -> loc.copy(vertical = loc.vertical - cmd.units)
                    Direction.Down -> loc.copy(vertical = loc.vertical + cmd.units)
                }
            }
    }

    part2 {
        input.fold(Location()) { loc, cmd ->
            when (cmd.direction) {
                Direction.Forward ->
                    loc.copy(
                        horizontal = loc.horizontal + cmd.units,
                        vertical = loc.vertical + (loc.aim * cmd.units))
                Direction.Up -> loc.copy(aim = loc.aim - cmd.units)
                Direction.Down -> loc.copy(aim = loc.aim + cmd.units)
            }
        }
    }
}
