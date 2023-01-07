import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toFrequencyMap

fun main() {
    val input = readResourceAsString("/day10.txt")

    val parsed = input.parseLines { it.toInt() }
    val outlet = 0
    val unitAdapter = parsed.max() + 3

    val adapters = (parsed + outlet + unitAdapter).toSet()

    part1 {
        val frequencies = adapters.sorted().windowed(2).map { (a, b) -> b - a }.toFrequencyMap()
        frequencies.getValue(1) * frequencies.getValue(3)
    }

    part2 {
        val memo = mutableMapOf<Int, Long>()
        fun findAdapterPaths(adapterJolts: Int): Long {
            if (memo[adapterJolts] != null) {
                return memo[adapterJolts]!!
            }

            if (adapterJolts == unitAdapter) {
                return 1
            }

            return adapters
                .filter { it in adapterJolts + 1..adapterJolts + 3 }
                .sumOf { findAdapterPaths(it) }
                .also { memo[adapterJolts] = it }
        }

        findAdapterPaths(outlet)
    }
}
