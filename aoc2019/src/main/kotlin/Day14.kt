import kotlin.math.ceil
import utils.clamp
import utils.filterNotBlank
import utils.mapFirst
import utils.mapSecond
import utils.match
import utils.merge
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toPair
import utils.twoParts

private data class QuantityAndElement(val quantity: Long, val element: String) {
    operator fun times(times: Long) = copy(quantity = quantity * times)
    operator fun plus(by: Long) = copy(quantity = quantity + by)
    operator fun minus(by: Long) = copy(quantity = quantity - by)
}

private fun parseQuantityAndElement(input: String): QuantityAndElement {
    return input
        .match("""([0-9]+) ([A-Z]+)""")
        .toPair()
        .mapFirst { it.toLong() }
        .let { (q, e) -> QuantityAndElement(q, e) }
}

private data class Production(val input: List<QuantityAndElement>, val output: QuantityAndElement)

private fun parseProduction(input: String): Production {
    val (rawInput, rawOutput) = input.twoParts(" => ")

    val output = parseQuantityAndElement(rawOutput)
    val input = rawInput.split(",").filterNotBlank().map { parseQuantityAndElement(it) }

    return Production(input, output)
}

private class Nanofactory(rules: List<Production>) {
    val rulesByOutputElement = rules.associateBy { it.output.element }

    fun oreCostsFor(
        requested: QuantityAndElement,
        stockpile: Map<String, Long> = emptyMap()
    ): Pair<Long, Map<String, Long>> {
        if (requested.quantity == 0L) {
            return Pair(0, stockpile)
        }

        if (requested.element == "ORE") {
            return Pair(requested.quantity, stockpile)
        }

        val rule =
            rulesByOutputElement[requested.element]
                ?: error("Could not find rule for ${requested.element}")

        val numReactions = ceil(requested.quantity / rule.output.quantity.toDouble()).toLong()
        val overproduction = (rule.output * numReactions) - requested.quantity

        val reactionResult =
            rule.input.fold(Pair(0L, stockpile)) { (currentCosts, currentStockpile), input ->
                val need = input * numReactions
                val (newStockpile, newInput) = satisfyFromStockpile(currentStockpile, need)
                val result = oreCostsFor(newInput, newStockpile)

                Pair(currentCosts + result.first, result.second)
            }

        return reactionResult.mapSecond {
            it.merge(mapOf(overproduction.element to overproduction.quantity), Long::plus)
        }
    }

    private fun satisfyFromStockpile(
        stockpile: Map<String, Long>,
        it: QuantityAndElement,
    ): Pair<Map<String, Long>, QuantityAndElement> {
        val newStockpile = stockpile.toMutableMap()
        val quantityInStockpile = stockpile[it.element] ?: 0L
        val takeFromStockpile = it.quantity.clamp(0L, quantityInStockpile)

        newStockpile.merge(it.element, takeFromStockpile, Long::minus)

        return Pair(newStockpile.toMap(), it - takeFromStockpile)
    }
}

fun main() {
    val input = readResourceAsString("/day14.txt")
    val parsed = input.parseLines { parseProduction(it) }

    part1 {
        val nanofactory = Nanofactory(parsed)
        nanofactory.oreCostsFor(QuantityAndElement(1, "FUEL")).first
    }
    part2 {
        val nanofactory = Nanofactory(parsed)

        val oreForOneFuel = nanofactory.oreCostsFor(QuantityAndElement(1, "FUEL"))
        var lowerBound = 1000000000000 / oreForOneFuel.first
        var upperBound = lowerBound * 2

        while (true) {
            val middle = lowerBound + ((upperBound - lowerBound) / 2)

            val result = nanofactory.oreCostsFor(QuantityAndElement(middle, "FUEL")).first
            if (result > 1000000000000) {
                upperBound = middle
            } else if (result < 1000000000000) {
                lowerBound = middle
            }
            if (upperBound - lowerBound <= 1 || result == 1000000000000) {
                break
            }
        }

        lowerBound
    }
}
