import algorithms.repeat
import datastructures.Graph
import datastructures.LeftRightDirection
import datastructures.LeftRightDirection.Left
import datastructures.LeftRightDirection.Right
import utils.*

private fun parseDay08Instructions(str: String) =
    str.map {
        when (it) {
            'L' -> Left
            'R' -> Right
            else -> unreachable(it.toString())
        }
    }

typealias Day08Graph = Graph<String, Unit, LeftRightDirection>

typealias Day08GraphNode = Graph<String, Unit, LeftRightDirection>.Node

private fun createDay08Graph(str: String) =
    Day08Graph().also { graph ->
        str.lines().forEach { line ->
            val (node, left, right) =
                line.match("(.+) = \\((.+), (.+)\\)").toList().map { graph.upsertNode(it, Unit) }

            graph.addEdge(node, left, Left)
            graph.addEdge(node, right, Right)
        }
    }

private fun calculateDistance(
    from: Day08GraphNode,
    instructions: Sequence<LeftRightDirection>,
    endCondition: (Day08GraphNode) -> Boolean
): Int {
    val cycled = instructions.cycle().iterator()

    return repeat(from) { node ->
            val next = cycled.next()
            if (endCondition(node)) {
                stop()
            } else {
                next(node.edges.find { it.data == next }!!.to)
            }
        }
        .iterations
}

fun main() {
    part1 {
        val input = readResourceAsString("/day08.txt")

        val (instructions, graph) =
            input
                .twoParts("\n\n")
                .mapFirst { parseDay08Instructions(it) }
                .mapSecond { createDay08Graph(it) }

        calculateDistance(graph.getNode("AAA"), instructions.cycle()) { it.label() == "ZZZ" }
    }

    part2 {
        val input = readResourceAsString("/day08.txt")
        val (instructions, graph) =
            input
                .twoParts("\n\n")
                .mapFirst { parseDay08Instructions(it) }
                .mapSecond { createDay08Graph(it) }

        val startNodes = graph.nodes.filter { it.label().last() == 'A' }

        startNodes
            .map { start ->
                calculateDistance(start, instructions.cycle()) { it.label().last() == 'Z' }
            }
            .map { it.toLong() }
            .reduce(Long::lcm)
    }
}
