package utils

fun String.twoParts(char: String): List<String> = this.split(char).expectSize(2)

fun String.twoParts(char: Char): List<String> = this.split(char).expectSize(2)

fun String.isLowercase(): Boolean = this.lowercase() == this

fun String.isUppercase(): Boolean = this.uppercase() == this

fun String.wrap(str: String): String = "${str}${this}${str}"

fun <T> String.twoParts(char: Char, block: (String) -> T): Pair<T, T> =
    this.twoParts(char).map(block).let { (a, b) -> Pair(a, b) }

fun String.match(regex: String): MatchResult.Destructured =
    regex.toRegex().find(this)?.destructured ?: error("$regex did not match $this")

fun MatchResult.Destructured.toPair(): Pair<String, String> = this.toList().toPair()

fun <T> String.parseLines(parser: (String) -> T): List<T> =
    this.lines().filterNotBlank().map(parser)
