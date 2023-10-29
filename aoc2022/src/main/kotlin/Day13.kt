import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separated
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
import utils.Test
import utils.filterNotBlank
import utils.map
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.runTests
import utils.twoParts

private enum class RightOrder {
    YES,
    NO,
    UNDECIDED
}

private sealed class Packet {
    class PacketList(val packets: List<Packet>) : Packet() {
        override fun toString() = "[${packets.joinToString(",")}]"
    }

    class PacketLiteral(val number: Int) : Packet() {
        override fun toString() = number.toString()
    }

    fun inRightOrder(other: Packet): RightOrder {
        when {
            this is PacketLiteral && other is PacketLiteral ->
                return when {
                    this.number == other.number -> RightOrder.UNDECIDED
                    this.number > other.number -> RightOrder.NO
                    this.number < other.number -> RightOrder.YES
                    else -> error("Unreachable")
                }
            this is PacketList && other is PacketList -> {
                this.packets.forEachIndexed { index, packet ->
                    val right = other.packets.getOrNull(index) ?: return RightOrder.NO

                    when (packet.inRightOrder(right)) {
                        RightOrder.YES -> return RightOrder.YES
                        RightOrder.NO -> return RightOrder.NO
                        RightOrder.UNDECIDED -> Unit // Continue
                    }
                }

                return if (this.packets.size == other.packets.size) {
                    RightOrder.UNDECIDED
                } else {
                    RightOrder.YES
                }
            }
            this is PacketLiteral -> return PacketList(listOf(this)).inRightOrder(other)
            other is PacketLiteral -> return this.inRightOrder(PacketList(listOf(other)))
            else -> error("Unreachable")
        }
    }
}

private val grammar =
    object : Grammar<Packet>() {
        val literal by regexToken("""\d+""")
        val lpar by literalToken("[")
        val comma by literalToken(",")
        val rpar by literalToken("]")
        val list by
            (-lpar * optional(separated(parser(this::expr), comma)) * -rpar) use
                {
                    Packet.PacketList(this?.terms ?: emptyList())
                }

        val expr: Parser<Packet> by (literal use { Packet.PacketLiteral(text.toInt()) }) or (list)

        override val rootParser by expr
    }

private fun parsePacket(input: String): Packet = grammar.parseToEnd(input)

private fun parsePacketPair(input: String): Pair<Packet, Packet> =
    input.twoParts('\n').map { grammar.parseToEnd(it) }

fun main() {
    runTests()

    val input = readResourceAsString("/day13.txt")
    part1 {
        val pairs = input.split("\n\n").filterNotBlank().map { parsePacketPair(it) }

        pairs
            .mapIndexed { index, pair ->
                if (pair.first.inRightOrder(pair.second) == RightOrder.YES) {
                    index + 1
                } else {
                    0
                }
            }
            .sum()
    }

    part2 {
        val dividers =
            listOf(
                parsePacket("[[2]]"),
                parsePacket("[[6]]"),
            )

        val packets = input.parseLines { parsePacket(it) } + dividers

        val orderedAndStringified =
            packets
                .sortedWith { o1, o2 ->
                    when (o1.inRightOrder(o2)) {
                        RightOrder.YES -> -1
                        RightOrder.NO -> 1
                        RightOrder.UNDECIDED -> error("Unreachable")
                    }
                }
                .map { it.toString() }

        val firstMarker = orderedAndStringified.indexOf("[[2]]") + 1
        val secondMarker = orderedAndStringified.indexOf("[[6]]") + 1

        firstMarker * secondMarker
    }
}

@Test
fun parseExample() {
    parsePacket("[[1],[2,3,4]]").toString() shouldBe "[[1],[2,3,4]]"
}

@Test
fun parsePacketPairTest() {
    val sample =
        """
        [1,1,3,1,1]
        [1,1,5,1,1]
    """
            .trimIndent()

    val result = parsePacketPair(sample)

    result.first.toString() shouldBe "[1,1,3,1,1]"
    result.second.toString() shouldBe "[1,1,5,1,1]"
}

@Test
fun firstSample() {
    data class Sample(val a: String, val b: String, val c: RightOrder)

    val examples =
        listOf(
            Sample("[1,1,3,1,1]", "[1,1,5,1,1]", RightOrder.YES),
            Sample("[[1],[2,3,4]]", "[[1],4]", RightOrder.YES),
            Sample("[9]", "[[8,7,6]]", RightOrder.NO),
            Sample("[[4,4],4,4]", "[[4,4],4,4,4]", RightOrder.YES),
            Sample("[7,7,7,7]", "[7,7,7]", RightOrder.NO),
            Sample("[]", "[3]", RightOrder.YES),
            Sample("[[[]]]", "[[]]", RightOrder.NO),
            Sample("[1,[2,[3,[4,[5,6,7]]]],8,9]", "[1,[2,[3,[4,[5,6,0]]]],8,9]", RightOrder.NO),
        )

    examples.forEach { (a, b, expected) ->
        val result = Pair(parsePacket(a), parsePacket(b))

        result.first.inRightOrder(result.second) shouldBe expected
    }
}
