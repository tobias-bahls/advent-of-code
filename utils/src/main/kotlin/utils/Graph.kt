package utils

class Graph<N, E> {
    private val _nodes = mutableListOf<Node>()
    val nodes
        get(): List<Node> = _nodes

    inner class Edge(val to: Node, val data: E) {
        fun label(): String {
            return if (data == Unit) "" else data.toString()
        }
    }
    inner class Node(val id: String, val data: N, private var _edges: MutableList<Edge>) {
        val edges
            get(): List<Edge> = _edges

        val neighbours
            get(): List<Node> = edges.map { it.to }

        fun addEdge(to: Node, data: E) {
            _edges.add(Edge(to, data))
        }

        fun label(): String {
            return if (data == Unit) id else "$id, $data"
        }

        override fun toString(): String {
            return "Node(id='$id', data=$data)"
        }
    }

    fun addNode(id: String, data: N): Node {
        val node = Node(id, data, mutableListOf())
        _nodes.add(node)
        return node
    }

    fun upsertNode(id: String, data: N) = findNode(id) ?: addNode(id, data)

    fun findNode(id: String): Node? = _nodes.find { it.id == id }
    fun getNode(id: String): Node = findNode(id) ?: error("Could not find node with id [$id]")

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
                listOf("${node.id} [label=\"${node.label()}\"]") +
                    node.edges.map { edge ->
                        "${node.id} -> ${edge.to.id} [label=\"${edge.label()}\"]"
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
}

typealias DumbGraph = Graph<Unit, Unit>

typealias DumbNode = Graph<Unit, Unit>.Node
