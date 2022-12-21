import utils.filterNotBlank
import utils.match
import utils.matchOrNull
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.singleMatch
import utils.toPair

private class EvaluationContext {
    private val expressions = mutableMapOf<String, MonkeyExpression>()

    fun getVariable(name: String): MonkeyExpression? = expressions[name]

    fun setVariable(name: String, expression: MonkeyExpression) {
        expressions[name] = expression
    }

    fun removeVariable(name: String) {
        expressions.remove(name)
    }
}

private sealed interface MonkeyExpression {
    interface BiOp : MonkeyExpression {
        val lhs: MonkeyExpression
        val rhs: MonkeyExpression

        override fun contains(expr: MonkeyExpression) =
            if (expr == this) true else lhs.contains(expr) || rhs.contains(expr)
    }

    data class Equals(override val lhs: MonkeyExpression, override val rhs: MonkeyExpression) :
        BiOp {
        override fun evaluate(ctx: EvaluationContext): MonkeyExpression =
            Equals(lhs.evaluate(ctx), rhs.evaluate(ctx))

        override fun toString() = "$lhs = $rhs"
    }

    data class Plus(override val lhs: MonkeyExpression, override val rhs: MonkeyExpression) : BiOp {
        override fun evaluate(ctx: EvaluationContext): MonkeyExpression {
            val lhs = lhs.evaluate(ctx)
            val rhs = rhs.evaluate(ctx)

            return if (lhs is Literal && rhs is Literal) {
                Literal(lhs.literal + rhs.literal)
            } else {
                Plus(lhs, rhs)
            }
        }
        override fun toString() = "($lhs + $rhs)"
    }

    data class Minus(override val lhs: MonkeyExpression, override val rhs: MonkeyExpression) :
        BiOp {

        override fun evaluate(ctx: EvaluationContext): MonkeyExpression {
            val lhs = lhs.evaluate(ctx)
            val rhs = rhs.evaluate(ctx)

            return if (lhs is Literal && rhs is Literal) {
                Literal(lhs.literal - rhs.literal)
            } else {
                Minus(lhs, rhs)
            }
        }
        override fun toString() = "($lhs - $rhs)"
    }
    data class Times(override val lhs: MonkeyExpression, override val rhs: MonkeyExpression) :
        BiOp {

        override fun evaluate(ctx: EvaluationContext): MonkeyExpression {
            val lhs = lhs.evaluate(ctx)
            val rhs = rhs.evaluate(ctx)

            return if (lhs is Literal && rhs is Literal) {
                Literal(lhs.literal * rhs.literal)
            } else {
                Times(lhs, rhs)
            }
        }
        override fun toString() = "($lhs * $rhs)"
    }
    data class Divided(override val lhs: MonkeyExpression, override val rhs: MonkeyExpression) :
        BiOp {

        override fun evaluate(ctx: EvaluationContext): MonkeyExpression {
            val lhs = lhs.evaluate(ctx)
            val rhs = rhs.evaluate(ctx)

            return if (lhs is Literal && rhs is Literal) {
                Literal(lhs.literal / rhs.literal)
            } else {
                Divided(lhs, rhs)
            }
        }

        override fun toString() = "($lhs / $rhs)"
    }
    data class Literal(val literal: Long) : MonkeyExpression {
        override fun evaluate(ctx: EvaluationContext): MonkeyExpression = this
        override fun toString() = literal.toString()
    }
    data class Variable(val name: String) : MonkeyExpression {
        override fun evaluate(ctx: EvaluationContext): MonkeyExpression =
            (ctx.getVariable(name) ?: Unresolved(name)).evaluate(ctx)

        override fun toString() = name
    }
    data class Unresolved(val name: String) : MonkeyExpression {
        override fun evaluate(ctx: EvaluationContext): MonkeyExpression = this

        override fun toString() = "?$name?"
    }

    fun evaluate(ctx: EvaluationContext): MonkeyExpression

    fun contains(expr: MonkeyExpression) = expr == this
}

private fun parseMonkeyExpression(input: String): MonkeyExpression {
    val literal = input.matchOrNull("""^(-?\d+)$""")?.singleMatch()?.toLong()
    if (literal != null) {
        return MonkeyExpression.Literal(literal)
    }

    val (lhs, op, rhs) = input.match("""(\w+) (\+|\-|/|\*) (\w+)""")

    val lhsExpr = MonkeyExpression.Variable(lhs)
    val rhsExpr = MonkeyExpression.Variable(rhs)

    return when (op) {
        "+" -> MonkeyExpression.Plus(lhsExpr, rhsExpr)
        "-" -> MonkeyExpression.Minus(lhsExpr, rhsExpr)
        "*" -> MonkeyExpression.Times(lhsExpr, rhsExpr)
        "/" -> MonkeyExpression.Divided(lhsExpr, rhsExpr)
        else -> error("Unknown operation: $op")
    }
}

private fun parseExpressionsToEvaluationContext(input: String): EvaluationContext {
    val ctx = EvaluationContext()
    input
        .parseLines { it.split(":").filterNotBlank().toPair() }
        .forEach { (name, expr) -> ctx.setVariable(name, parseMonkeyExpression(expr)) }

    return ctx
}

fun main() {
    val input = readResourceAsString("/day21.txt")

    part1 {
        val ctx = parseExpressionsToEvaluationContext(input)
        val root = ctx.getVariable("root") ?: error("Could not find root")

        val result = root.evaluate(ctx)
        check(result is MonkeyExpression.Literal)
        result.literal
    }

    part2 {
        val ctx = parseExpressionsToEvaluationContext(input)
        ctx.removeVariable("humn")

        val root = ctx.getVariable("root") ?: error("Could not find root")
        check(root is MonkeyExpression.BiOp)

        val newRoot = MonkeyExpression.Equals(root.lhs, root.rhs)
        ctx.removeVariable("root")
        ctx.setVariable("root", newRoot)

        val result = newRoot.evaluate(ctx)

        fun rearrange(
            equals: MonkeyExpression.Equals,
        ): MonkeyExpression.Equals {
            val expression = equals.lhs
            check(expression is MonkeyExpression.BiOp)
            val equalsRight = equals.rhs

            val (literalSide, expressionSide) =
                if (expression.lhs is MonkeyExpression.Literal) {
                    (expression.lhs to expression.rhs)
                } else {
                    (expression.rhs to expression.lhs)
                }

            val newRhs =
                when (expression) {
                    is MonkeyExpression.Plus -> MonkeyExpression.Minus(equalsRight, literalSide)
                    is MonkeyExpression.Times -> MonkeyExpression.Divided(equalsRight, literalSide)
                    is MonkeyExpression.Minus ->
                        when (literalSide) {
                            expression.lhs -> MonkeyExpression.Minus(literalSide, equalsRight)
                            expression.rhs -> MonkeyExpression.Plus(equalsRight, literalSide)
                            else -> error("Unreachable")
                        }
                    is MonkeyExpression.Divided ->
                        when (literalSide) {
                            expression.lhs -> MonkeyExpression.Divided(literalSide, equalsRight)
                            expression.rhs -> MonkeyExpression.Times(equalsRight, literalSide)
                            else -> error("Unreachable")
                        }
                    else -> error("Can't solve $expression")
                }

            return MonkeyExpression.Equals(expressionSide, newRhs)
        }

        fun solveEquals(equals: MonkeyExpression.Equals): MonkeyExpression.Equals {
            var current = equals

            while (current.lhs is MonkeyExpression.BiOp) {
                current = rearrange(current)
            }

            return current
        }

        solveEquals(result as MonkeyExpression.Equals)
            .let { it.evaluate(ctx) as MonkeyExpression.Equals }
            .let { it.rhs as MonkeyExpression.Literal }
            .literal
    }
}
