package intcode

@JvmInline value class Address(val address: Int)

fun Int.toAddress() = Address(this)

fun parseIntcodeProgram(raw: String) = raw.trim().split(",").map { it.toInt() }
