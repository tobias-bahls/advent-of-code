package utils

fun IntRange.fullyContains(other: IntRange) = this.first >= other.first && this.last <= other.last

fun IntRange.overlaps(other: IntRange) = this.intersect(other).isNotEmpty()

fun ClosedRange<Int>.extend(by: Int) = IntRange(start, endInclusive).extend(by)

fun IntRange.extend(by: Int) = (start - by..endInclusive + by)

fun LongRange.offset(offs: Long) = (start + offs..endInclusive + offs)

fun LongRange.fastIntersection(other: LongRange) =
    maxOf(this.first, other.first)..minOf(this.last, other.last)

fun parseClosedIntRange(raw: String) =
    raw.match("""(\d+)-(\d+)""").toPair().map { it.toInt() }.let { (a, b) -> a..b }
