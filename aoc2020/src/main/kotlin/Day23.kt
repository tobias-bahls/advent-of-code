import utils.part1
import utils.part2
import utils.readResourceAsString

private data class Cup(val label: Int) {
    lateinit var next: Cup
}

private class Cups(cups: List<Int>) {
    var currentCup: Cup
    val minCup: Int = cups.min()
    val maxCup: Int = cups.max()
    val numCups: Int = cups.size
    val cupsByLabel: Map<Int, Cup>

    init {
        currentCup = Cup(cups.first())

        val cupsByLabel = mutableMapOf<Int, Cup>()
        cupsByLabel[currentCup.label] = currentCup

        var current = currentCup
        for (cup in cups.drop(1)) {
            val next = Cup(cup)
            cupsByLabel[cup] = next
            current.next = next
            current = next
        }

        current.next = currentCup
        this.cupsByLabel = cupsByLabel
    }

    fun move() {
        val startOfRemoval = currentCup.next
        val endOfRemoval = skipNodes(startOfRemoval, 2)

        currentCup.next = endOfRemoval.next

        val removeLabels = slice(startOfRemoval, endOfRemoval).map { it.label }
        val destinationCup = determineDestinationCup(currentCup.label, removeLabels)

        val destinationNode = findNode(destinationCup)
        val oldNext = destinationNode.next
        destinationNode.next = startOfRemoval
        endOfRemoval.next = oldNext

        currentCup = currentCup.next
    }

    private fun determineDestinationCup(currentCup: Int, removeLabels: List<Int>): Int {
        var destinationCupLabel = (currentCup - 1 % numCups)
        while (destinationCupLabel in removeLabels || destinationCupLabel < minCup) {
            destinationCupLabel = (destinationCupLabel - 1).let { if (it < minCup) maxCup else it }
        }

        return destinationCupLabel
    }

    private fun skipNodes(cup: Cup, times: Int) = cupSequence(cup).drop(times).first()

    private fun slice(start: Cup, end: Cup) =
        cupSequence(start).takeWhile { it != end.next }.toList()

    fun findNode(label: Int) = cupsByLabel[label]!!

    fun cupSequence() = cupSequence(currentCup)

    fun cupSequence(start: Cup): Sequence<Cup> {
        var current = start
        return generateSequence {
            val result = current
            current = current.next

            result
        }
    }
}

private fun parseCups(input: String): List<Int> = input.trim().map { it.digitToInt() }

fun main() {
    val input = readResourceAsString("/day23.txt")

    part1 {
        val cups = Cups(parseCups(input))
        repeat(100) { cups.move() }

        cups
            .cupSequence()
            .dropWhile { it.label != 1 }
            .drop(1)
            .take(cups.numCups - 1)
            .joinToString("") { it.label.toString() }
    }
    part2 {
        val parsed = parseCups(input)
        val newCups = parsed + (parsed.max() + 1..1_000_000)

        val cups = Cups(newCups)

        repeat(10_000_000) { cups.move() }

        cups
            .cupSequence()
            .dropWhile { it.label != 1 }
            .drop(1)
            .take(2)
            .map { it.label.toLong() }
            .reduce(Long::times)
    }
}
