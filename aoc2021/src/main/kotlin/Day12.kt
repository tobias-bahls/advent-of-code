import datastructures.DumbGraph
import utils.filterNotBlank
import utils.isLowercase
import utils.isUppercase
import utils.map
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.twoParts

fun buildGraph(sample: String): DumbGraph {
    val graph = DumbGraph()

    sample.lines().filterNotBlank().map { line ->
        val (from, to) = line.twoParts("-").map { graph.upsertNode(it, Unit) }

        graph.addBidirectionalEdge(from, to, Unit)
    }
    return graph
}

fun main() {
    val input = readResourceAsString("/day12.txt")
    val graph = buildGraph(input)

    val start = graph.findNode("start") ?: error("should have start node")
    val end = graph.findNode("end") ?: error("should have end node")

    part1 {
        graph
            .depthFirstTraversal(start, end) { node, visited ->
                node.neighbours.filter { it.id.isUppercase() || it !in visited }
            }
            .size
    }

    part2 {
        graph
            .depthFirstTraversal(start, end) { node, visited ->
                node.neighbours.filter { neighbour ->
                    when {
                        neighbour.id == "start" -> false
                        neighbour.id.isUppercase() -> true
                        neighbour.id.isLowercase() -> {
                            if (neighbour !in visited) {
                                return@filter true
                            }

                            val hasVisitedLowercaseTwice =
                                visited
                                    .filter { it.id.isLowercase() }
                                    .groupingBy { it.id }
                                    .eachCount()
                                    .filter { it.value > 1 }
                                    .isNotEmpty()

                            return@filter !hasVisitedLowercaseTwice
                        }
                        else -> error("Unreachable")
                    }
                }
            }
            .size
    }
}
