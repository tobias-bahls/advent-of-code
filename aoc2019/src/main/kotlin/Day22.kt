import java.math.BigInteger
import utils.*

sealed interface Day22Instruction {
    fun apply(list: List<Int>): List<Int>

    data object DealIntoNewStack : Day22Instruction {
        override fun apply(list: List<Int>): List<Int> = list.reversed()
    }

    data class Cut(val n: Int) : Day22Instruction {
        override fun apply(list: List<Int>): List<Int> =
            when {
                n > 0 -> list.drop(n) + list.take(n)
                n < 0 -> list.takeLast(n * -1) + list.dropLast(n * -1)
                else -> error("can't cut by 0")
            }
    }

    data class DealWithIncrement(val n: Int) : Day22Instruction {
        override fun apply(list: List<Int>): List<Int> {
            val newList = MutableList(list.size) { 0 }

            var cursor = 0
            list.forEach {
                newList[cursor] = it
                cursor = (cursor + n).mod(list.size)
            }

            return newList
        }
    }
}

private fun parseInstruction(input: String) =
    when {
        input == "deal into new stack" -> Day22Instruction.DealIntoNewStack
        input.startsWith("deal with increment") ->
            input.match("increment (-?\\d+)$").let { (n) ->
                Day22Instruction.DealWithIncrement(n.toInt())
            }
        input.startsWith("cut") ->
            input.match("cut (-?\\d+)$").let { (n) -> Day22Instruction.Cut(n.toInt()) }
        else -> error("Can't parse $input")
    }

fun main() {
    val input = readResourceAsString("/day22.txt")
    val instructions = input.parseLines { parseInstruction(it) }

    part1 {
        val startDeck = (0 until 10007).toList()
        val result = instructions.fold(startDeck) { acc, ins -> ins.apply(acc) }

        result.indexOf(2019)
    }

    part2 {
        val mem = arrayOf(BigInteger.ONE, BigInteger.ZERO)
        val numCards = 119315717514047.toBigInteger()
        val numShuffles = 101741582076661.toBigInteger()

        instructions.reversed().forEach {
            when (it) {
                Day22Instruction.DealIntoNewStack -> {
                    mem[0] = mem[0].negate()
                    mem[1] = (mem[1].inc()).negate()
                }
                is Day22Instruction.Cut -> {
                    mem[1] += it.n.toBigInteger()
                }
                is Day22Instruction.DealWithIncrement -> {
                    val pow = it.n.toBigInteger().modPow(numCards - 2.toBigInteger(), numCards)
                    mem[0] *= pow
                    mem[1] *= pow
                }
            }

            mem[0] %= numCards
            mem[1] %= numCards
        }

        val pow = mem[0].modPow(numShuffles, numCards)
        ((pow * 2020.toBigInteger()) +
                ((mem[1] * (pow + numCards.dec())) *
                    ((mem[0].dec()).modPow(numCards - 2.toBigInteger(), numCards))))
            .mod(numCards)
    }
}
