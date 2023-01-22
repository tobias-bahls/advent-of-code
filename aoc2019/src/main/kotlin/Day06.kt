import datastructures.Tree
import utils.mapValues
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toPair

private fun parseInputToTree(input: String): Tree<Unit> {
    val parsed =
        input.parseLines { it.split(")").toPair() }.groupingBy { it.first }.mapValues { it.second }

    fun buildTree(current: Tree<Unit>.Node) {
        val children = parsed[current.id] ?: return

        children.forEach {
            val child = current.addChild(it, Unit)
            buildTree(child)
        }
    }

    val tree = Tree<Unit>()
    val root = tree.addRoot("COM", Unit)
    buildTree(root)
    return tree
}

fun main() {
    val input = readResourceAsString("/day06.txt")
    val tree = parseInputToTree(input)

    part1 { tree.traversePreOrder().map { it.depth }.sum() }
    part2 {
        val you = tree.findNode("YOU") ?: error("Could not find YOU node")
        val san = tree.findNode("SAN") ?: error("Could not find SAN node")
        val commonAncestor =
            tree.firstCommonAncestor(you, san)?.id
                ?: error("No common ancestor between YOU and SAN")

        val relevantNodes = setOf("YOU", "SAN", commonAncestor)
        val depths =
            tree
                .traversePreOrder()
                .filter { it.node.id in relevantNodes }
                .associate { it.node.id to it.depth }

        (depths.getValue("YOU") - 1 - depths.getValue(commonAncestor)) +
            (depths.getValue("SAN") - 1 - depths.getValue(commonAncestor))
    }
}
