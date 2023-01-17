import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString

private fun calculateFuelRequirement(mass: Int) = (mass / 3) - 2

fun main() {
    val input = readResourceAsString("/day01.txt")
    val parsed = input.parseLines { it.toInt() }

    part1 { parsed.sumOf { calculateFuelRequirement(it) } }
    part2 {
        parsed.sumOf { module ->
            var total = 0
            var requirement = calculateFuelRequirement(module)
            while (requirement >= 0) {
                total += requirement
                requirement = calculateFuelRequirement(requirement)
            }

            total
        }
    }
}
