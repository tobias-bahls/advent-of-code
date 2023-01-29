package utils

import kotlin.math.absoluteValue
import kotlin.math.pow

fun Int.times() = (1..this)

fun Int.pow(n: Int) = this.toDouble().pow(n).toInt()

fun Long.pow(n: Int) = this.toDouble().pow(n).toLong()

fun Int.clamp(min: Int, max: Int) = this.coerceAtMost(max).coerceAtLeast(min)

fun Long.clamp(min: Long, max: Long) = this.coerceAtMost(max).coerceAtLeast(min)

fun Int.gcd(other: Int): Int =
    if (other == 0) {
        this
    } else {
        other.gcd(this % other)
    }

fun Long.lcm(other: Long): Long {
    return if (this == 0L || other == 0L) {
        0
    } else {
        ((this / this.gcd(other)) * other).absoluteValue
    }
}

fun Long.gcd(other: Long): Long =
    if (other == 0L) {
        this
    } else {
        other.gcd(this % other)
    }

fun Int.lcm(other: Int): Int {
    return if (this == 0 || other == 0) {
        0
    } else {
        ((this / this.gcd(other)) * other).absoluteValue
    }
}

fun Int.oneBasedModulo(n: Int) = ((this - 1) % n) + 1

fun Long.setBit(n: Int, to: Boolean): Long =
    if (to) {
        this or (1L shl n)
    } else {
        this and (1L shl n).inv()
    }
