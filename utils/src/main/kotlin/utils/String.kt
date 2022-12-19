package utils

fun String.twoParts(char: String): Pair<String, String> =
    this.split(char).expectSize(2).filterNotBlank().toPair()

fun String.twoParts(char: Char): Pair<String, String> =
    this.split(char).expectSize(2).filterNotBlank().toPair()

fun String.isLowercase(): Boolean = this.lowercase() == this

fun String.isUppercase(): Boolean = this.uppercase() == this

fun String.wrap(str: String): String = "${str}${this}${str}"

fun <T> String.twoParts(char: Char, block: (String) -> T): Pair<T, T> =
    this.twoParts(char).map(block).let { (a, b) -> Pair(a, b) }

fun String.match(regex: String): MatchResult.Destructured =
    regex.toRegex().find(this)?.destructured ?: error("$regex did not match $this")

fun String.matchOrNull(regex: String): MatchResult.Destructured? =
    regex.toRegex().find(this)?.destructured

fun MatchResult.Destructured.toPair(): Pair<String, String> = this.toList().toPair()

fun MatchResult.Destructured.singleMatch(): String = this.toList().expectSize(1).first()

fun MatchResult.Destructured?.singleMatchOrNull(): String? = this?.toList()?.firstOrNull()

fun <T> String.parseLines(parser: (String) -> T): List<T> =
    this.lines().filterNotBlank().map(parser)
