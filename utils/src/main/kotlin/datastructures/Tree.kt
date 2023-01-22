package datastructures

data class Traversal<N>(val node: Tree<N>.Node, val depth: Int)

open class Tree<N> {
    private val idGen: IDGenerator = IDGenerator()
    private lateinit var rootNode: Node

    val root
        get() = rootNode

    open inner class Node(
        open val parent: Node?,
        val id: String,
        val data: N,
        private val _children: MutableList<ChildNode> = mutableListOf()
    ) {

        val children
            get(): List<ChildNode> = _children

        fun addChild(id: String?, data: N): ChildNode {
            val child = ChildNode(this, id ?: idGen.generate(), data)
            this._children.add(child)
            return child
        }

        fun parents(): Sequence<Node> {
            var current: Tree<N>.Node? = this

            return generateSequence {
                val elem = current ?: return@generateSequence null

                current = elem.parent

                elem
            }
        }

        fun findChild(predicate: (N) -> Boolean): ChildNode? =
            children.find { predicate(it.data!!) }

        override fun toString(): String {
            return "Node(id=$id, data=$data, children=$children)"
        }
    }

    inner class ChildNode(override val parent: Node, id: String, data: N) : Node(parent, id, data)

    fun addRoot(id: String?, data: N): Node {
        rootNode = Node(null, id ?: idGen.generate(), data)

        return rootNode
    }

    fun findNode(nodeId: String) = allNodes().find { it.id == nodeId }

    fun traversePreOrder(): Sequence<Traversal<N>> {
        val queue = ArrayDeque<Traversal<N>>()

        queue.addFirst(Traversal(root, 0))

        return generateSequence {
            if (queue.isEmpty()) {
                return@generateSequence null
            }
            val current = queue.removeFirst()

            current.node.children.forEach { queue.addFirst(Traversal(it, current.depth + 1)) }

            current
        }
    }

    fun firstCommonAncestor(a: Node, b: Node): Node? {
        val aParents = a.parents().toList()
        val bParents = b.parents().toList()

        return aParents.intersect(bParents.toSet()).firstOrNull()
    }

    fun allNodes(): List<Node> = traversePreOrder().map { it.node }.toList()

    fun prettyPrint(): String {
        val builder = StringBuilder()

        traversePreOrder().forEach { (node, depth) ->
            builder.append("  ".repeat(depth))
            builder.append("- ${node.id} (${node.data})\n")
        }

        return builder.toString()
    }
}
