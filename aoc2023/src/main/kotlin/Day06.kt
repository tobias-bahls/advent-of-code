import utils.*

fun main() {

    fun distanceTravelled(held: Long, time: Long) = held * time

    fun countWaysToWin(time: Long, distance: Long) =
        (0 until time).count { distanceTravelled(it, time - it) > distance }

    part1 {
        val input = readResourceAsString("/day06.txt")
        val races =
            input
                .split("\n")
                .map { line ->
                    line.split(":").last().split(" ").filterNotBlank().map { it.toLong() }
                }
                .let { (a, b) -> a.zip(b) }

        races.map { (time, distance) -> countWaysToWin(time, distance) }.reduce(Int::times)
    }

    part2 {
        val input = readResourceAsString("/day06.txt")
        val (time, distance) =
            input.twoParts("\n").map { line -> line.split(":").last().replace(" ", "").toLong() }

        countWaysToWin(time, distance)
    }
}
