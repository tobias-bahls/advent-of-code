import io.kotest.matchers.collections.shouldContainExactly
import utils.Test
import utils.indexOfOrNull
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.runTests

private data class GroveCoordinate(val originalIndex: Int, val number: Long)

private class GroveCoordinates(originalList: List<Long>, encryptionKey: Int = 1) {
    private val initialList = originalList.map { it * encryptionKey }
    private val currentList = mutableListOf<GroveCoordinate>()

    val currentNumbers
        get() = currentList.map { it.number }

    init {
        currentList += initialList.mapIndexed { index, it -> GroveCoordinate(index, it) }
    }

    fun move(originalIndex: Int, by: Long) {
        val toMove =
            currentList.indexOfOrNull { it.originalIndex == originalIndex }
                ?: error("Could not find element with original index $originalIndex")

        val relativeOffset = toMove + by
        val newPosition =
            if (relativeOffset == 0L) {
                currentList.size - 1
            } else {
                (toMove + by).mod(currentList.size - 1)
            }

        val removed = currentList.removeAt(toMove)
        currentList.add(newPosition, removed)
    }

    fun mix() {
        initialList.forEachIndexed { index, originalNum -> move(index, originalNum) }
    }

    fun nthCoordinate(n: Int): Long {
        val zero = currentList.indexOfOrNull { it.number == 0L } ?: error("Could not find zero")
        val mod = (zero + n).mod(currentList.size)
        return currentList[mod].number
    }
}

fun main() {
    runTests()
    val input = readResourceAsString("/day20.txt")

    part1 {
        val numbers = GroveCoordinates(input.parseLines { it.toLong() })
        numbers.mix()

        listOf(1000, 2000, 3000).map { numbers.nthCoordinate(it) }.reduce(Long::plus)
    }

    part2 {
        val numbers = GroveCoordinates(input.parseLines { it.toLong() }, 811589153)
        repeat(10) { numbers.mix() }

        listOf(1000, 2000, 3000).map { numbers.nthCoordinate(it) }.reduce(Long::plus)
    }
}

@Test
fun firstExample() {
    val intList = GroveCoordinates(listOf(4, 5, 6, 1, 7, 8, 9))
    intList.move(3, 1)
    intList.currentNumbers shouldContainExactly listOf(4, 5, 6, 7, 1, 8, 9)
}

@Test
fun secondExample() {
    val intList = GroveCoordinates(listOf(4, -2, 5, 6, 7, 8, 9))
    intList.move(1, -2)
    intList.currentNumbers shouldContainExactly listOf(4, 5, 6, 7, 8, -2, 9)
}

@Test
fun moveToIndex0() {
    val intList = GroveCoordinates(listOf(1, 2, -2, -3, 0, 3, 4))
    intList.move(2, -2)

    intList.currentNumbers shouldContainExactly listOf(1, 2, -3, 0, 3, 4, -2)
}
