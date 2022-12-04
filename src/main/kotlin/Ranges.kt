fun IntRange.fullyContains(other: IntRange) = this.first >= other.first && this.last <= other.last

fun IntRange.overlaps(other: IntRange) = this.intersect(other).isNotEmpty()
