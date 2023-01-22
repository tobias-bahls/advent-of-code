package utils

fun <T, K, R> Grouping<T, K>.mapValues(block: (T) -> R) =
    fold(emptyList<R>()) { acc, it -> acc + block(it) }
