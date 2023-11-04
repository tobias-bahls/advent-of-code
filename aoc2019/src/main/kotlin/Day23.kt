import intcode.IntcodeInterpreter
import intcode.InterpreterStatus
import intcode.parseIntcodeProgram
import utils.part1
import utils.part2
import utils.readResourceAsString

private data class Day23Packet(val src: Int, val dst: Int, val x: Long, val y: Long)

private class NetworkedIntcodeComputer(
    val index: Int,
    private val interpreter: IntcodeInterpreter
) {
    private val receiveBuffer = mutableListOf<Day23Packet>()

    init {
        interpreter.apply {
            run()
            check(status == InterpreterStatus.WAITING_FOR_INPUT)
            addInput(index.toLong())
            run()
        }
    }

    fun receive(packet: Day23Packet) {
        receiveBuffer.add(packet)
    }

    fun wake(): Boolean {
        val received =
            if (receiveBuffer.isEmpty()) {
                interpreter.addInput(-1)
                false
            } else {
                val packet = receiveBuffer.removeFirst()

                interpreter.addInput(listOf(packet.x, packet.y))
                true
            }

        interpreter.run()
        return received
    }

    fun readSentPackets(): List<Day23Packet> {
        return interpreter.readOutput().windowed(3, 3).map { (dst, x, y) ->
            Day23Packet(index, dst.toInt(), x, y)
        }
    }
}

private class Network(private val computers: List<NetworkedIntcodeComputer>) {
    private val computersById = computers.associateBy { it.index }
    val processedPackets = mutableListOf<Day23Packet>()
    private var natPacket: Day23Packet? = null

    fun step() {
        val sentPackets = computers.flatMap { it.readSentPackets() }

        sentPackets.forEach { processPacket(it) }

        val didWork = computers.map { it.wake() }.any { it }
        if (sentPackets.isEmpty() && !didWork && natPacket != null) {
            processPacket(natPacket!!.copy(dst = 0))
        }
    }

    private fun processPacket(packet: Day23Packet) {
        processedPackets += packet

        if (packet.dst == 255) {
            natPacket = packet
            return
        }

        getComputer(packet.dst).receive(packet)
    }

    private fun getComputer(id: Int): NetworkedIntcodeComputer =
        computersById[id] ?: error("Computer with id $id not found")
}

private fun initializeNetwork(): Network {
    val input = readResourceAsString("/day23.txt")
    val program = parseIntcodeProgram(input)
    val computers =
        (0 until 50).map { index -> NetworkedIntcodeComputer(index, IntcodeInterpreter(program)) }
    return Network(computers)
}

fun main() {
    part1 {
        val network = initializeNetwork()

        while (true) {
            network.step()
            val packetTo255 = network.processedPackets.find { it.dst == 255 }
            if (packetTo255 != null) {
                return@part1 packetTo255.y
            }
        }
    }

    part2 {
        val network = initializeNetwork()

        while (true) {
            network.step()
            val firstDoublePacket =
                network.processedPackets
                    .asSequence()
                    .filter { it.dst == 0 }
                    .map { it.y }
                    .sorted()
                    .windowed(2)
                    .firstOrNull { (y1, y2) -> y1 == y2 }

            if (firstDoublePacket != null) {
                return@part2 firstDoublePacket.first()
            }
        }
    }
}
