package intcode

fun parseIntcodeProgram(raw: String) = raw.replace("\n", "").trim().split(",").map { it.toLong() }
