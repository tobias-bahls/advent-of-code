import algorithms.solveUsingChineseRemainderTheorem
import utils.filterNotBlank
import utils.mapFirst
import utils.mapSecond
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toPair

fun main() {
    val input = readResourceAsString("/day13.txt")
    val (earliestDeparture, buses) =
        input
            .lines()
            .filterNotBlank()
            .toPair()
            .mapFirst { it.toInt() }
            .mapSecond { s ->
                s.split(",")
                    .filterNotBlank()
                    .withIndex()
                    .filter { it.value != "x" }
                    .map { IndexedValue(it.index, it.value.toInt()) }
            }

    part1 {
        buses
            .map { it.value }
            .map { Pair(it, it - (earliestDeparture % it)) }
            .minBy { it.second }
            .let { it.first * it.second }
    }

    part2 {
        val ns = buses.map { it.value.toBigInteger() }
        val `as` = buses.map { (it.index * -1).mod(it.value).toBigInteger() }

        solveUsingChineseRemainderTheorem(ns, `as`)
    }
}
