package utils

fun <A, B, RA, RB> Pair<A, B>.transform(block: (Pair<A, B>) -> Pair<RA, RB>): Pair<RA, RB> =
    block(this)

fun <A, R> Pair<A, A>.map(block: (A) -> R): Pair<R, R> = Pair(block(this.first), block(this.second))

fun <A, B, R> Pair<A, B>.mapFirst(block: (A) -> R): Pair<R, B> =
    Pair(block(this.first), this.second)

fun <A, B, R> Pair<A, B>.mapSecond(block: (B) -> R): Pair<A, R> =
    Pair(this.first, block(this.second))

fun <A, B, C> Pair<Pair<A, B>, C>.toTriple(): Triple<A, B, C> {
    val (a, b) = this.first
    val c = this.second

    return Triple(a, b, c)
}
