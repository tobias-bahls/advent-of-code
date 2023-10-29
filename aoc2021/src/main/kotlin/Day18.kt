import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import io.kotest.matchers.shouldBe
import kotlin.math.ceil
import kotlin.math.floor
import utils.Test
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.runTests

private sealed class SnailfishNumber {

    lateinit var parent: Pair

    class Literal constructor(var number: Int) : SnailfishNumber() {
        override fun toString(): String = number.toString()

        override fun magnitude(): Int = number
    }

    class Pair constructor(var left: SnailfishNumber, var right: SnailfishNumber) :
        SnailfishNumber() {

        init {
            left.parent = this
            right.parent = this
        }

        override fun toString(): String = "[$left,$right]"

        fun replaceChild(toReplace: SnailfishNumber, replacement: SnailfishNumber) {
            replacement.parent = this

            when {
                left == toReplace -> left = replacement
                right == toReplace -> right = replacement
                else -> error("Tried to replace non-existing child $toReplace in $this")
            }
        }

        override fun magnitude(): Int = (left.magnitude() * 3) + (right.magnitude() * 2)
    }

    operator fun plus(other: SnailfishNumber): Pair {
        val result = Pair(this.clone(), other.clone())

        while (true) {
            if (result.explode()) {
                continue
            }

            if (result.split()) {
                continue
            }

            break
        }

        return result
    }

    fun explode(): Boolean {
        val toExplode =
            findAllTermsAtDepth(4).filterIsInstance<Pair>().firstOrNull() ?: return false

        val terms = findAllTerms()
        val explodeIdx = terms.indexOf(toExplode)

        terms
            .filterIndexed { idx, _ -> idx < explodeIdx }
            .filterIsInstance<Literal>()
            .lastOrNull { it.parent != toExplode }
            ?.also { it.number += (toExplode.left as Literal).number }

        terms
            .filterIndexed { idx, _ -> idx > explodeIdx }
            .filterIsInstance<Literal>()
            .firstOrNull { it.parent != toExplode }
            ?.also { it.number += (toExplode.right as Literal).number }

        toExplode.parent.replaceChild(toExplode, Literal(0))

        return true
    }

    fun split(): Boolean {
        val toSplit =
            findAllTerms().filterIsInstance<Literal>().firstOrNull { it.number >= 10 }
                ?: return false

        val divided = toSplit.number / 2.0
        val replacement = Pair(Literal(floor(divided).toInt()), Literal(ceil(divided).toInt()))

        toSplit.parent.replaceChild(toSplit, replacement)

        return true
    }

    fun findAllTermsAtDepth(depth: Int): List<SnailfishNumber> {
        if (depth == 0) {
            return listOf(this)
        }

        return if (this is Pair) {
            left.findAllTermsAtDepth(depth - 1) + right.findAllTermsAtDepth(depth - 1)
        } else {
            listOf()
        }
    }

    fun findAllTerms(): List<SnailfishNumber> {
        if (this !is Pair) {
            return listOf(this)
        }

        return left.findAllTerms() + right.findAllTerms() + listOf(this)
    }

    abstract fun magnitude(): Int

    private fun clone(): SnailfishNumber = parseSnailfishNumber(toString())
}

private val grammar =
    object : Grammar<SnailfishNumber>() {
        val literal by regexToken("""\d+""")
        val lpar by literalToken("[")
        val comma by literalToken(",")
        val rpar by literalToken("]")
        val pair by
            (-lpar * parser(this::expr) * -comma * parser(this::expr) * -rpar).map { (lhs, rhs) ->
                SnailfishNumber.Pair(lhs, rhs)
            }
        val expr: Parser<SnailfishNumber> by
            (literal use { SnailfishNumber.Literal(text.toInt()) }) or (pair)

        override val rootParser by expr
    }

private fun parseSnailfishNumber(input: String): SnailfishNumber {
    return grammar.parseToEnd(input)
}

fun main() {
    runTests()

    val input = readResourceAsString("/day18.txt")

    part1 {
        input.parseLines { parseSnailfishNumber(it) }.reduce(SnailfishNumber::plus).magnitude()
    }

    part2 {
        val numbers = input.parseLines { parseSnailfishNumber(it) }

        val combinations = numbers.flatMap { a -> numbers.map { b -> Pair(a, b) } }

        combinations
            .flatMap { (a, b) ->
                listOf(
                    (a + b).magnitude(),
                    (b + a).magnitude(),
                )
            }
            .max()
    }
}

@Test
private fun testExplode() {
    mapOf(
            "[[[[[9,8],1],2],3],4]" to "[[[[0,9],2],3],4]",
            "[7,[6,[5,[4,[3,2]]]]]" to "[7,[6,[5,[7,0]]]]",
            "[[6,[5,[4,[3,2]]]],1]" to "[[6,[5,[7,0]]],3]",
            "[[3,[2,[1,[7,3]]]],[6,[5,[4,[3,2]]]]]" to "[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]",
            "[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]" to "[[3,[2,[8,0]]],[9,[5,[7,0]]]]")
        .map { (k, v) -> parseSnailfishNumber(k) to parseSnailfishNumber(v) }
        .forEach { (input, expected) ->
            input.explode()

            input.toString() shouldBe expected.toString()
        }
    println("✅ Explode tests")
}

@Test
private fun testSplit() {
    mapOf("[11,1]" to "[[5,6],1]")
        .map { (k, v) -> parseSnailfishNumber(k) to parseSnailfishNumber(v) }
        .forEach { (input, expected) ->
            input.split()
            input.toString() shouldBe expected.toString()
        }

    println("✅ Split tests")
}

@Test
private fun testReduction() {
    val a = parseSnailfishNumber("[[[[4,3],4],4],[7,[[8,4],9]]]")
    val b = parseSnailfishNumber("[1,1]")

    (a + b).toString() shouldBe "[[[[0,7],4],[[7,8],[6,0]]],[8,1]]"

    println("✅ Reduction test")
}

@Test
private fun testSumOfSums1() {
    val sample =
        """
    [1,1]
    [2,2]
    [3,3]
    [4,4]
    """
            .trimIndent()

    sample.parseLines { parseSnailfishNumber(it) }.reduce(SnailfishNumber::plus).toString() shouldBe
        "[[[[1,1],[2,2]],[3,3]],[4,4]]"

    println("✅ Sum of sums #1")
}

@Test
private fun testSumOfSums2() {
    val sample =
        """
            [1,1]
            [2,2]
            [3,3]
            [4,4]
            [5,5]
            [6,6]
    """
            .trimIndent()

    sample.parseLines { parseSnailfishNumber(it) }.reduce(SnailfishNumber::plus).toString() shouldBe
        "[[[[5,0],[7,4]],[5,5]],[6,6]]"

    println("✅ Sum of sums #2")
}

@Test
private fun testComplexSumOfSums() {
    val sample =
        """[[[0,[4,5]],[0,0]],[[[4,5],[2,6]],[9,5]]]
[7,[[[3,7],[4,3]],[[6,3],[8,8]]]]
[[2,[[0,8],[3,4]]],[[[6,7],1],[7,[1,6]]]]
[[[[2,4],7],[6,[0,5]]],[[[6,8],[2,8]],[[2,1],[4,5]]]]
[7,[5,[[3,8],[1,4]]]]
[[2,[2,2]],[8,[8,1]]]
[2,9]
[1,[[[9,3],9],[[9,0],[0,7]]]]
[[[5,[7,4]],7],1]
[[[[4,2],2],6],[8,7]]"""
            .trimIndent()

    sample
        .parseLines { parseSnailfishNumber(it) }
        .map { it }
        .reduce(SnailfishNumber::plus)
        .toString() shouldBe "[[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]]"

    println("✅ Complex sum of sums")
}

@Test
private fun magnitude() {
    mapOf(
            "[[1,2],[[3,4],5]]" to 143,
            "[[[[0,7],4],[[7,8],[6,0]]],[8,1]]" to 1384,
            "[[[[1,1],[2,2]],[3,3]],[4,4]]" to 445,
            "[[[[3,0],[5,3]],[4,4]],[5,5]]" to 791,
            "[[[[5,0],[7,4]],[5,5]],[6,6]]" to 1137,
            "[[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]]" to 3488)
        .mapKeys { (k, _) -> parseSnailfishNumber(k) }
        .forEach { (example, expected) -> example.magnitude() shouldBe expected }

    println("✅ Magnitude")
}
