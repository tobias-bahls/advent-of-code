fun List<String>.filterNotBlank() = this.filter { it.isNotBlank() }
fun List<String>.mapInts() = this.map { it.toInt() }
fun <T> List<T>.expectSize(expectedSize: Int): List<T> {
    check(this.size == expectedSize) { "Size of list was ${this.size}, expected $expectedSize "}
    return this
}
fun <T> T.dump(): T  = this.also { println(this) }
