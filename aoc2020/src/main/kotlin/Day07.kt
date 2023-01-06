import utils.match
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.regexParts
import utils.visitAllNodes

private data class BagSpec(val color: String)

private data class ContainedBag(val count: Int, val spec: BagSpec)

private data class Bag(val spec: BagSpec, val contained: List<ContainedBag>) {
    operator fun contains(spec: BagSpec) = contained.any { it.spec == spec }
}

private data class BagCollection(val bags: List<Bag>) {
    val bagsBySpec = bags.associateBy { it.spec }

    fun bagForSpec(spec: BagSpec) = bagsBySpec[spec] ?: error("Could not find bag for spec $spec")
}

private fun parseBag(input: String): Bag {
    val (thisBag) = input.match("""(.*) bags contain""")
    val containedBags =
        input.regexParts("""\d+ .*? bags?[.,]""").map {
            val (count, spec) = it.match("""(\d+) (.*?) bag""")

            ContainedBag(count.toInt(), BagSpec(spec))
        }

    return Bag(BagSpec(thisBag), containedBags)
}

fun main() {
    val input = readResourceAsString("/day07.txt") // sample
    val bagCollection = BagCollection(input.parseLines { parseBag(it) })
    val shinyGold = BagSpec("shiny gold")

    part1 {
        val seen =
            visitAllNodes(listOf(shinyGold)) { current ->
                bagCollection.bags.filter { current in it }.map { it.spec }
            }
        seen.size - 1
    }

    part2 {
        val shinyGoldBag = bagCollection.bagForSpec(shinyGold)

        fun countContained(bag: Bag): Int =
            bag.contained.sumOf { contained ->
                val containedBag = bagCollection.bagForSpec(contained.spec)

                contained.count + (contained.count * countContained(containedBag))
            }

        countContained(shinyGoldBag)
    }
}
