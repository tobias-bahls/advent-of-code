package utils

fun IntRange.fullyContains(other: IntRange) = this.first >= other.first && this.last <= other.last

fun IntRange.overlaps(other: IntRange) = this.intersect(other).isNotEmpty()

fun ClosedRange<Int>.extend(by: Int) = IntRange(start, endInclusive).extend(by)

fun IntRange.extend(by: Int) = (start - by..endInclusive + by)
