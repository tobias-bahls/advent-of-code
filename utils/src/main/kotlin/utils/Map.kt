package utils

fun <T> MutableMap<T, Int>.increment(key: T, by: Int = 1) = merge(key, by, Int::plus)

fun <T> MutableMap<T, Long>.increment(key: T, by: Long = 1) = merge(key, by, Long::plus)
