import org.jgrapht.alg.StoerWagnerMinimumCut
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import utils.*

fun main() {
    part1 {
        val input = readResourceAsString("/day25.txt")

        val graph = SimpleGraph<String, DefaultEdge>(DefaultEdge::class.java)
        input.parseLines { line ->
            val (from, tos) = line.twoParts(": ")
            tos.split(" ").forEach { to ->
                graph.addVertex(from)
                graph.addVertex(to)
                graph.addEdge(from, to)
            }
        }

        val minCut = StoerWagnerMinimumCut(graph)
        val oneSide = minCut.minCut()
        val otherSide = graph.vertexSet() - oneSide

        oneSide.size * otherSide.size
    }
}
