import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import utils.Part
import utils.Part.PART1
import utils.Part.PART2
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString

class HomeworkExpressionEvaluator(part: Part) : Grammar<Long>() {
    private val literal by regexToken("""\d+""")
    private val lpar by literalToken("(")
    private val rpar by literalToken(")")
    private val plus by literalToken("+")
    private val times by literalToken("*")
    @Suppress("unused") private val ws by regexToken("\\s+", ignore = true)

    private val plusOrTimes = (plus or times).map { it.type }

    private val opGroup: Parser<Long> by
        (-lpar * parser(this::rootParser) * -rpar).map { it } or literal.map { it.text.toLong() }

    private val equalPrecedenceChain: Parser<Long> by
        leftAssociative(opGroup, plusOrTimes) { a, op, b -> if (op == plus) a + b else a * b }

    private val plusChain: Parser<Long> by leftAssociative(opGroup, plus) { a, _, b -> a + b }
    private val timesChain: Parser<Long> by leftAssociative(plusChain, times) { a, _, b -> a * b }

    override val rootParser by
        when (part) {
            PART1 -> equalPrecedenceChain
            PART2 -> timesChain
        }
}

fun main() {
    val input = readResourceAsString("/day18.txt")

    part1 {
        val evaluator = HomeworkExpressionEvaluator(PART1)
        input.parseLines { evaluator.parseToEnd(it) }.sum()
    }
    part2 {
        val evaluator = HomeworkExpressionEvaluator(PART2)
        input.parseLines { evaluator.parseToEnd(it) }.sum()
    }
}
