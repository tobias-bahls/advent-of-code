package utils

@JvmInline
value class BinaryString(val string: String) {
    companion object {
        fun fromHexString(input: String) =
            input
                .chunked(1)
                .joinToString("") { it.toInt(16).toString(2).padStart(4, '0') }
                .let { BinaryString(it) }
    }

    fun readAtOffset(len: Int, offset: Int) = string.slice(offset..len - 1 + offset)

    fun readInteger(len: Int, offset: Int) = readAtOffset(len, offset).toInt(2)

    fun readBoolean(offset: Int) = readAtOffset(1, offset).toInt() == 1

    fun reader(): BinaryStringReader = BinaryStringReader(this)
}

class BinaryStringReader(private val str: BinaryString) {
    var offset = 0

    fun readInteger(len: Int) = str.readInteger(len, offset).also { offset += len }

    fun readBoolean() = str.readBoolean(offset).also { offset += 1 }

    fun readRaw(len: Int): String = str.readAtOffset(len, offset).also { offset += len }
}
