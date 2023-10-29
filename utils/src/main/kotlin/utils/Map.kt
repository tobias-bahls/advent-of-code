package utils

fun <T> MutableMap<T, Int>.increment(key: T, by: Int = 1) = merge(key, by, Int::plus)

fun <T> MutableMap<T, Long>.increment(key: T, by: Long = 1) = merge(key, by, Long::plus)

@JvmName("mergeMutableMap")
fun <K, V> MutableMap<K & Any, V & Any>.merge(
    other: Map<K & Any, V & Any>,
    combine: (V, V) -> V
): MutableMap<K & Any, V & Any> {
    other.forEach { (k, v) -> this.merge(k, v, combine) }

    return this
}

fun <K, V> Map<K & Any, V & Any>.merge(
    other: Map<K & Any, V & Any>,
    combine: (V, V) -> V
): Map<K & Any, V & Any> = this.toMutableMap().merge(other, combine).toMap()
