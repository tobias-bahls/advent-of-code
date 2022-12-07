package datastructures

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

        fun addChild(id: String?, data: N) {
            val child = ChildNode(this, id ?: idGen.generate(), data)
            this._children.add(child)
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

    fun traversePreOrder(block: (Node, Int) -> Unit) {
        fun traverse(currentDepth: Int, node: Node) {
            block(node, currentDepth)
            node.children.forEach { traverse(currentDepth + 1, it) }
        }

        traverse(0, root)
    }

    fun allNodes(): List<Node> {
        val result = mutableListOf<Node>()

        traversePreOrder { node, _ -> result.add(node) }

        return result
    }
    fun prettyPrint(): String {
        var builder = StringBuilder()

        traversePreOrder { node, depth ->
            builder.append("  ".repeat(depth))
            builder.append("- ${node.id} (${node.data})\n")
        }

        return builder.toString()
    }
}
