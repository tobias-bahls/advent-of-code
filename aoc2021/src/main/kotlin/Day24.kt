import java.util.Stack
import kotlin.math.absoluteValue
import utils.filterNotBlank
import utils.part1
import utils.part2
import utils.readResourceAsString

private data class CorrelatedCodeBlocks(val a: CodeBlock, val b: CodeBlock) {
    val x = b.offset + a.check
}

private data class CodeBlock(val digit: Int, val check: Int, val offset: Int) {
    val pushes = check > 0
}

private fun parseCodeBlock(index: Int, block: String): CodeBlock {
    val lines = block.lines()
    val check = lines[4].replace("add x ", "").toInt()
    val offset = lines[14].replace("add y ", "").toInt()

    return CodeBlock(index, check, offset)
}

private fun correlateCodeBlocks(codeBlocks: List<CodeBlock>): List<CorrelatedCodeBlocks> {
    val stack = Stack<CodeBlock>()
    return codeBlocks.mapNotNull {
        if (it.pushes) {
            stack.push(it)
            null
        } else {
            val popped = stack.pop()
            CorrelatedCodeBlocks(it, popped)
        }
    }
}

fun main() {
    val input = readResourceAsString("/day24.txt")
    val codeBlocks =
        input.split("inp w").filterNotBlank().mapIndexed { index, codeBlock ->
            parseCodeBlock(index, codeBlock)
        }
    val correlated = correlateCodeBlocks(codeBlocks)

    part1 {
        correlated
            .flatMap {
                if (it.x > 0) {
                    listOf(Pair(it.a.digit, 9), Pair(it.b.digit, 9 - it.x))
                } else {
                    listOf(Pair(it.a.digit, 9 + it.x), Pair(it.b.digit, 9))
                }
            }
            .sortedBy { it.first }
            .joinToString("") { it.second.toString() }
    }

    part2 {
        correlated
            .flatMap {
                if (it.x > 0) {
                    listOf(Pair(it.a.digit, 1 + it.x), Pair(it.b.digit, 1))
                } else {
                    listOf(Pair(it.a.digit, 1), Pair(it.b.digit, 1 + it.x.absoluteValue))
                }
            }
            .sortedBy { it.first }
            .joinToString("") { it.second.toString() }
    }
}
