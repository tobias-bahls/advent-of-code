import utils.parseLines
import utils.part1
import utils.readResourceAsString
import utils.reduceTimes

private fun bruteForceLoopSize(publicKey: Long, subjectNumber: Long): Long {
    var current = 0L
    var currentPKey = 1L
    while (true) {
        current += 1

        currentPKey = (currentPKey * subjectNumber) % 20201227L
        if (currentPKey == publicKey) {
            return current
        }
    }
}

private fun transformSubjectNumber(subjectNumber: Long, loopSize: Long) =
    reduceTimes(loopSize, 1L) { current -> (current * subjectNumber) % 20201227L }

fun main() {
    val input = readResourceAsString("/day25.txt")
    val (cardPkey, doorPkey) = input.parseLines { it.toLong() }

    part1 {
        val cardLoopSize = bruteForceLoopSize(cardPkey, 7)

        transformSubjectNumber(doorPkey, cardLoopSize)
    }
}
