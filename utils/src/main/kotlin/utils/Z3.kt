package utils

import com.microsoft.z3.*
import java.math.BigDecimal

class Z3 {
    val ctx = Context()
    val solver = ctx.mkSolver()

    val Long.real
        get() = ctx.mkReal(this)!!

    val BigDecimal.real
        get() = this.toLong().real

    operator fun <A : ArithSort, B : ArithSort> BigDecimal.plus(b: Expr<B>) =
        ctx.mkAdd(this.real, b)!!

    operator fun <A : ArithSort, B : ArithSort> Expr<A>.plus(b: Expr<B>) = ctx.mkAdd(this, b)!!

    operator fun <A : ArithSort, B : ArithSort> Expr<A>.times(b: Expr<B>) = ctx.mkMul(this, b)!!

    infix fun <A : Sort, B : Sort> Expr<A>.eq(b: Expr<B>) = ctx.mkEq(this, b)!!

    operator fun <T> invoke(block: Z3.() -> T): T = block(this)

    fun real(name: String) = ctx.mkRealConst(name)!!
}

fun <T> z3(block: Z3.() -> T) = Z3()(block)

operator fun <T : Sort> Model.get(expr: Expr<T>) = getConstInterp(expr)!!

fun Solver.checkAndAssert() = check(check() == Status.SATISFIABLE) { "not satisfiable" }

fun <T : Sort> Expr<T>.intoBigDecimal() = BigDecimal(this.toString())
