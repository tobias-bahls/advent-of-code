package algorithms

import java.math.BigInteger

/**
 * returns `x` so that:
 * ```
 * as[0] == x % ns[0]
 * as[1] == x % ns[1]
 * as[n] == x % ns[n]
 * ```
 */
fun solveUsingChineseRemainderTheorem(ns: List<BigInteger>, `as`: List<BigInteger>): BigInteger {
    val bigN = ns.reduce(BigInteger::times)
    return ns.zip(`as`)
        .fold(BigInteger.ZERO) { x, (n, a) ->
            val bigNk = bigN / n
            val bigNkp = bigNk.modInverse(n)

            x + (a * bigNk * bigNkp)
        }
        .mod(bigN)
}
