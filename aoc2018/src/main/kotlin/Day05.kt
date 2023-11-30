import algorithms.repeat
import utils.*

private fun react(polymer: String): String? {
    val reactIndex =
        polymer.asSequence().windowed(2).indexOfFirst { (a, b) ->
            when {
                a.lowercaseChar() != b.lowercaseChar() -> false
                a.isLowerCase() && b.isUpperCase() -> true
                a.isUpperCase() && b.isLowerCase() -> true
                else -> false
            }
        }

    return if (reactIndex == -1) {
        null
    } else {
        polymer.removeRange(reactIndex, reactIndex + 2)
    }
}

private fun reactUntilStable(polymer: String) =
    repeat(polymer) {
            when (val new = react(it)) {
                null -> stop()
                else -> next(new)
            }
        }
        .element

fun main() {
    val polymer = readResourceAsString("/day05.txt")
    xpart1 { reactUntilStable(polymer).length }

    part2 {
        ('a'..'z')
            .mapAsync {
                val candidate = polymer.replace(it.toString(), "", ignoreCase = true)
                reactUntilStable(candidate).length
            }
            .awaitAll()
            .min()
    }
}
