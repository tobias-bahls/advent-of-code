package datastructures

import algorithms.dijkstraPath
import utils.Scored

class Graph<I, N, E> {
    private val _nodes = mutableMapOf<I, Node>()
    val nodes
        get(): Set<Node> = _nodes.values.toSet()

    inner class Edge(val to: Node, val data: E) {
        fun label(): String {
            return if (data == Unit) "" else data.toString()
        }
    }

    inner class Node(val id: I, val data: N, private var _edges: MutableList<Edge>) {
        val edges
            get(): List<Edge> = _edges

        val neighbours
            get(): List<Node> = edges.map { it.to }

        fun addEdge(to: Node, data: E) {
            _edges.add(Edge(to, data))
        }

        fun label(): String {
            return if (data == Unit) id.toString() else "$id, $data"
        }

        override fun toString(): String {
            return "Node(id='$id', data=$data)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Graph<*, *, *>.Node

            return id == other.id
        }

        override fun hashCode(): Int {
            return id?.hashCode() ?: 0
        }
    }

    fun addNode(id: I, data: N): Node {
        val node = Node(id, data, mutableListOf())
        _nodes[id] = node
        return node
    }

    fun upsertNode(id: I, data: N) = findNode(id) ?: addNode(id, data)

    fun findNode(id: I): Node? = _nodes[id]

    fun getNode(id: I): Node = findNode(id) ?: error("Could not find node with id [$id]")

    fun addBidirectionalEdge(node1: Node, node2: Node, data: E) {
        node1.addEdge(to = node2, data = data)
        node2.addEdge(to = node1, data = data)
    }

    fun addEdge(node1: Node, node2: Node, data: E) {
        node1.addEdge(to = node2, data = data)
    }

    fun asGraphvizDot(): String {
        val nodeDescription =
            nodes.flatMap { node ->
                listOf("\"${node.id}\" [label=\"${node.label()}\"]") +
                    node.edges.map { edge ->
                        "\"${node.id}\" -> \"${edge.to.id}\" [label=\"${edge.label()}\"]"
                    }
            }

        return (listOf("digraph G {") + nodeDescription + listOf("}")).joinToString("\n")
    }

    fun depthFirstTraversal(
        start: Node,
        end: Node,
        determineNeighbours: (Node, List<Node>) -> List<Node>
    ): List<List<Node>> {
        fun inner(node: Node, visited: List<Node>): List<List<Node>> {
            if (node.id == end.id) {
                return listOf(visited)
            }

            val toVisit = determineNeighbours(node, visited)
            if (toVisit.isEmpty()) {
                return listOf()
            }

            return toVisit.flatMap { inner(it, visited + it) }
        }

        return inner(start, listOf(start))
    }

    fun dijkstra(start: Graph<I, N, Int>.Node, end: Graph<I, N, Int>.Node) =
        dijkstraPath<Graph<I, N, Int>.Node> {
            this.start = start
            this.end = end

            neighbours { it.edges.map { edge -> Scored(edge.data, edge.to) } }
        }
}

typealias DumbGraph = Graph<String, Unit, Unit>

typealias DumbNode = Graph<String, Unit, Unit>.Node

fun <I, N> Graph<I, N, Int>.floydWarshall():
    Map<Graph<I, N, Int>.Node, Map<Graph<I, N, Int>.Node, Int>> {
    val result = mutableMapOf<Graph<I, N, Int>.Node, MutableMap<Graph<I, N, Int>.Node, Int>>()

    nodes.forEach { node ->
        val thisNode = result[node] ?: mutableMapOf()

        node.edges.forEach { thisNode[it.to] = it.data }

        thisNode[node] = 0
        result[node] = thisNode
    }

    nodes.forEach { k ->
        val kNodes = result.getValue(k)
        nodes.forEach { i ->
            val iNodes = result.getValue(i)
            val ikDistance = iNodes[k]

            nodes.forEach { j ->
                val ijDistance = iNodes[j] ?: Integer.MAX_VALUE
                val kjDistance = kNodes[j]

                if (ikDistance != null &&
                    kjDistance != null &&
                    ijDistance > ikDistance + kjDistance) {

                    iNodes[j] = ikDistance + kjDistance
                }
            }
        }
    }

    return result
}
