import datastructures.Grid
import datastructures.Tile
import utils.applyEach
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.times
import utils.untilTrue
import utils.visitAllNodes

class Octopus(private var _energyLevel: Int) {
    fun increaseEnergyLevel() {
        _energyLevel = _energyLevel.inc()
    }

    fun resetEnergyLevel() {
        _energyLevel = 0
    }
    val flashing
        get() = _energyLevel > 9
}

fun step(grid: Grid<Octopus>): Collection<Tile<Octopus>> {
    grid.tiles.forEach { it.data.increaseEnergyLevel() }

    val flashing = grid.tiles.filter { it.data.flashing }

    val flashed =
        visitAllNodes(flashing) { tile ->
            tile.adjacent().applyEach { it.data.increaseEnergyLevel() }.filter { it.data.flashing }
        }

    flashed.forEach { it.data.resetEnergyLevel() }

    return flashed
}

fun main() {
    val input = readResourceAsString("/day11.txt")

    part1 {
        val grid = Grid(input) { Octopus(it.digitToInt()) }

        100.times().sumOf { step(grid).size }
    }
    part2 {
        val grid = Grid(input) { Octopus(it.digitToInt()) }

        // +1 because they started flashing in the step before the one that we're interested in.
        untilTrue { step(grid).size == grid.tiles.size } + 1
    }
}
