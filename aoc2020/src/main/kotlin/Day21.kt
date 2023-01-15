import utils.filterNotBlank
import utils.mapFirst
import utils.mapSecond
import utils.match
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toPair

private data class AllergenList(val ingredients: Set<String>, val allergens: Set<String>)

private fun parseAllergenList(input: String): AllergenList {
    val (ingredients, allergens) =
        input
            .match("""(.*) \(contains (.*)\)""")
            .toPair()
            .mapFirst { it.split(" ").toSet() }
            .mapSecond { it.split(",").filterNotBlank().toSet() }

    return AllergenList(ingredients, allergens)
}

private fun calculatePotentialIngredients(
    allergenList: List<AllergenList>
): Map<String, Set<String>> {
    return allergenList
        .flatMap { l -> l.allergens.map { it to l.ingredients } }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
        .mapValues { (_, value) -> value.reduce { acc, it -> acc.intersect(it) } }
}

fun main() {
    val input = readResourceAsString("/day21.txt")
    val parsed = input.parseLines { parseAllergenList(it) }
    val allIngredients = parsed.flatMap { it.ingredients }.toSet()

    part1 {
        val potentialIngredients = calculatePotentialIngredients(parsed)
        val definitelySafe = allIngredients - potentialIngredients.values.flatten().toSet()

        parsed.sumOf { it.ingredients.intersect(definitelySafe).size }
    }
    part2 {
        fun solve(
            matchedIngredients: Map<String, String>,
            potentialIngredients: Map<String, Set<String>>
        ): Map<String, String> {
            if (potentialIngredients.isEmpty()) {
                return matchedIngredients
            }

            val unambiguous =
                potentialIngredients.entries.find { it.value.size == 1 }
                    ?: error("Did not find any unambiguous ingredient")

            val allergen = unambiguous.value.single()

            val newMatchedIngredients = matchedIngredients + (unambiguous.key to allergen)
            val newPotential =
                potentialIngredients
                    .filterNot { it.key == unambiguous.key }
                    .mapValues { it.value.minus(allergen) }

            return solve(newMatchedIngredients, newPotential)
        }

        val matchedIngredients = solve(emptyMap(), calculatePotentialIngredients(parsed))

        matchedIngredients.entries.sortedBy { it.key }.joinToString(",") { it.value }
    }
}
