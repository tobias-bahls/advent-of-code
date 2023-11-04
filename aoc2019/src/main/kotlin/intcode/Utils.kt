package intcode

fun parseIntcodeProgram(raw: String) = raw.replace("\n", "").trim().split(",").map { it.toLong() }

fun List<Long>.asString() = map { it.toInt().toChar() }.joinToString("")
