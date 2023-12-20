import utils.*

private enum class Signal {
    HI,
    LO,
}

private data class SignalCommand(val signal: Signal, val from: String, val to: String)

private sealed interface Day20Module {
    val id: String
    val inputs: List<String>
    val outputs: List<String>

    fun receive(signal: Signal, from: String): List<SignalCommand>

    fun reset()
}

private data class FlipFlop(
    override val id: String,
    override val inputs: List<String>,
    override val outputs: List<String>,
) : Day20Module {
    init {
        reset()
    }

    private var state = false

    override fun receive(signal: Signal, from: String) =
        if (signal == Signal.LO) {
            state = !state
            outputs.map { SignalCommand(if (state) Signal.HI else Signal.LO, id, it) }
        } else {
            emptyList()
        }

    override fun reset() {
        state = false
    }
}

private data class Conjunction(
    override val id: String,
    override val inputs: List<String>,
    override val outputs: List<String>,
) : Day20Module {
    init {
        reset()
    }

    private lateinit var state: MutableMap<String, Signal>

    override fun receive(signal: Signal, from: String): List<SignalCommand> {
        state[from] = signal

        val outSignal =
            if (state.values.all { it == Signal.HI }) {
                Signal.LO
            } else {
                Signal.HI
            }

        return outputs.map { SignalCommand(outSignal, id, it) }
    }

    override fun reset() {
        state = inputs.associateWith { Signal.LO }.toMutableMap()
    }
}

private data class NoOpModule(
    override val id: String,
    override val inputs: List<String>,
    override val outputs: List<String>,
) : Day20Module {
    override fun receive(signal: Signal, from: String) = emptyList<SignalCommand>()

    override fun reset() = Unit
}

private data class Broadcast(
    override val id: String,
    override val inputs: List<String>,
    override val outputs: List<String>,
) : Day20Module {
    override fun receive(signal: Signal, from: String) =
        outputs.map { SignalCommand(signal, id, it) }

    override fun reset() = Unit
}

private fun buildModuleNetwork(str: String): Network {
    val connections =
        str.parseLines { line ->
                line.split(" -> ").toPair().mapSecond { it.split(",").filterNotBlank() }
            }
            .toMap()

    val shunts =
        connections.values
            .flatten()
            .filter { id -> id !in connections.keys.map { it.trim('&', '%') } }
            .associateWith { emptyList<String>() }

    return Network(
        (connections + shunts).map { (id, out) ->
            val normalizedId = id.trimStart('%', '&')
            val inputs =
                connections.entries
                    .filter { normalizedId in it.value }
                    .map { it.key.trimStart('%', '&') }

            when {
                id == "broadcaster" -> Broadcast(id, inputs, out)
                id.startsWith("%") -> FlipFlop(normalizedId, inputs, out)
                id.startsWith("&") -> Conjunction(normalizedId, inputs, out)
                else -> NoOpModule(id, inputs, out)
            }
        })
}

private class Network(val modules: List<Day20Module>) {
    val modulesById = modules.associateBy { it.id }

    fun pressButton(): List<SignalCommand> {
        val signals = mutableListOf(SignalCommand(Signal.LO, "button", "broadcaster"))
        var signalIndex = 0

        while (signalIndex < signals.size) {
            val signal = signals[signalIndex]
            val target = modulesById[signal.to] ?: error("unknwon module: ${signal.to}")

            val outs = target.receive(signal.signal, signal.from)
            signals.addAll(outs)
            signalIndex++
        }

        return signals
    }

    fun getModule(id: String): Day20Module = modulesById[id] ?: error("unknown module: $id")

    fun reset() = modules.forEach { it.reset() }
}

fun main() {
    part1 {
        val input = readResourceAsString("/day20.txt")
        val network = buildModuleNetwork(input)

        val allSignals =
            reduceTimes(1000, emptyList<SignalCommand>()) { it + network.pressButton() }

        allSignals.count { it.signal == Signal.LO } * allSignals.count { it.signal == Signal.HI }
    }

    part2 {
        val input = readResourceAsString("/day20.txt")
        val network = buildModuleNetwork(input)

        fun buttonPressesUntilCommand(signal: SignalCommand): Int {
            return repeatUntilTrue {
                val sent = network.pressButton()

                sent.any { it == signal }
            } + 1
        }

        val rxModule = network.getModule("rx")
        val rxInputModuleId = rxModule.inputs.single()
        val rxInputModule = network.getModule(rxInputModuleId)

        check(rxInputModule is Conjunction) { "Module $rxInputModule should be conjunction" }

        rxInputModule.inputs
            .map {
                network.reset()

                buttonPressesUntilCommand(SignalCommand(Signal.HI, it, rxInputModuleId))
            }
            .map { it.toLong() }
            .reduce(Long::lcm)
    }
}
