package utils

class IDGenerator {
    private var counter: Int = 0

    fun generate() = counter++.toString()
}
