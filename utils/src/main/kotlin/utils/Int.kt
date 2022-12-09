package utils

import kotlin.math.pow

fun Int.times() = (1..this)

fun Int.pow(n: Int) = this.toDouble().pow(n).toInt()

fun Int.clamp(min: Int, max: Int) = this.coerceAtMost(max).coerceAtLeast(min)
