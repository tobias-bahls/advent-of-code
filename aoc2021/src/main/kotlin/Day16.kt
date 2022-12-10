import OperatorType.*
import utils.BinaryString
import utils.BinaryStringReader
import utils.part1
import utils.part2
import utils.readResourceAsString

enum class OperatorType {
    SUM,
    PRODUCT,
    MIN,
    MAX,
    GT,
    LT,
    EQ
}

fun parseOperatorType(type: Int) =
    when (type) {
        0 -> SUM
        1 -> PRODUCT
        2 -> MIN
        3 -> MAX
        5 -> GT
        6 -> LT
        7 -> EQ
        else -> error("Unknown type: $type")
    }

sealed interface Packet {
    val version: Int
    data class Literal(override val version: Int, val value: Long) : Packet
    data class Operator(
        override val version: Int,
        val type: OperatorType,
        val subpackets: List<Packet>
    ) : Packet
}

fun parsePacket(reader: BinaryStringReader): Packet {
    val packetVersion = reader.readInteger(3)
    val packetType = reader.readInteger(3)

    return if (packetType == 4) {
        parseLiteralPacket(packetVersion, reader)
    } else {
        parseOperatorPacket(packetVersion, parseOperatorType(packetType), reader)
    }
}

fun parseLiteralPacket(packetVersion: Int, reader: BinaryStringReader): Packet.Literal {
    var shouldContinue = true
    var accum = ""
    while (shouldContinue) {
        shouldContinue = reader.readBoolean()
        accum += reader.readRaw(4)
        reader.offset
    }
    return Packet.Literal(packetVersion, accum.toLong(2))
}

sealed interface LengthType {
    data class Length(val length: Int) : LengthType
    data class Count(val count: Int) : LengthType
}

fun parseOperatorPacket(
    packetVersion: Int,
    type: OperatorType,
    reader: BinaryStringReader
): Packet.Operator {
    val mode =
        if (reader.readBoolean()) {
            LengthType.Count(reader.readInteger(11))
        } else {
            LengthType.Length(reader.readInteger(15))
        }

    val subpackets = mutableListOf<Packet>()
    if (mode is LengthType.Length) {
        val readUntil = reader.offset + mode.length

        while (reader.offset < readUntil) {
            subpackets.add(parsePacket(reader))
        }
    } else if (mode is LengthType.Count) {
        var read = 0
        while (read < mode.count) {
            subpackets.add(parsePacket(reader))
            read++
        }
    }

    return Packet.Operator(packetVersion, type, subpackets)
}

fun main() {
    val input = readResourceAsString("/day16.txt").trim()

    val parsed = BinaryString.fromHexString(input)

    part1 {
        val packet = parsePacket(parsed.reader())

        var result = 0
        fun addVersions(packet: Packet) {
            result += packet.version

            if (packet is Packet.Operator) {
                packet.subpackets.forEach { addVersions(it) }
            }
        }

        addVersions(packet)
        result
    }

    part2 {
        val packet = parsePacket(parsed.reader())

        fun evaluate(packet: Packet): Long {
            if (packet is Packet.Literal) {
                return packet.value
            }
            check(packet is Packet.Operator)
            val evaluated = packet.subpackets.map { evaluate(it) }
            return when (packet.type) {
                SUM -> evaluated.sum()
                PRODUCT -> evaluated.reduce(Long::times)
                MIN -> evaluated.min()
                MAX -> evaluated.max()
                GT ->
                    if (evaluated[0] > evaluated[1]) {
                        1
                    } else {
                        0
                    }
                LT ->
                    if (evaluated[0] < evaluated[1]) {
                        1
                    } else {
                        0
                    }
                EQ ->
                    if (evaluated[0] == evaluated[1]) {
                        1
                    } else {
                        0
                    }
            }
        }

        evaluate(packet)
    }
}
