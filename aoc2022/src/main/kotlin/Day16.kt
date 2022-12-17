import datastructures.Graph
import datastructures.floydWarshall
import utils.filterNotBlank
import utils.match
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString

private data class Valve(val name: String, val flowRate: Int, val connections: List<String>) {
    override fun toString(): String = name
}

private fun parseValve(input: String): Valve {
    val (id, flowRate, connections) =
        input.match("""Valve (\w+) has flow rate=(\d+); tunnels? leads? to valves? (.+)""")

    return Valve(id, flowRate.toInt(), connections.split(",").filterNotBlank())
}

private fun calculateDistances(valves: List<Valve>): Map<Valve, Map<Valve, Int>> {
    val valvesById = valves.associateBy { it.name }

    val graph = Graph<String, Valve, Int>()
    valves.forEach { valve ->
        val thisNode = graph.upsertNode(valve.name, valve)

        valve.connections.forEach {
            val neighbour = valvesById.getValue(it)
            val neighbourNode = graph.upsertNode(neighbour.name, neighbour)
            graph.addEdge(thisNode, neighbourNode, 1)
        }
    }

    val distances = graph.floydWarshall()

    val relevantValves = valves.filter { it.flowRate > 0 || it.name == "AA" }
    val relevantDistances =
        distances
            .filter { it.key.data in relevantValves }
            .mapKeys { it.key.data }
            .mapValues {
                it.value
                    .filter { other -> other.key.data in relevantValves }
                    .mapKeys { other -> other.key.data }
            }

    return relevantDistances
}

private data class StateCacheKey(
    val currentValve: Valve,
    val openValves: Set<Valve>,
    val time: Int
)

private data class State(
    val currentValve: Valve,
    val openValves: Set<Valve>,
    val currentFlow: Int,
    val time: Int,
) {
    fun key() = StateCacheKey(currentValve, openValves, time)

    fun currentValveIsOpen() = currentValve in openValves

    fun openValve(): State {
        val newTime = time - 1
        return copy(
            openValves = openValves + currentValve,
            time = newTime,
            currentFlow = currentFlow + (newTime * currentValve.flowRate))
    }

    fun move(valve: Valve, distance: Int): State {
        return copy(currentValve = valve, time = time - distance)
    }
}

private fun solve(valves: List<Valve>, time: Int): MutableMap<StateCacheKey, Int> {
    val valvesById = valves.associateBy { it.name }
    val relevantDistances = calculateDistances(valves)

    val best = mutableMapOf<StateCacheKey, Int>().withDefault { -1 }
    val queue = ArrayDeque<State>()

    fun addToQueue(state: State) {
        if (state.time == 0) {
            return
        }
        val key = state.key()
        if (state.currentFlow <= best.getValue(key)) {
            return
        }

        best[state.key()] = state.currentFlow
        queue.addLast(state)
    }

    queue.add(State(valvesById.getValue("AA"), emptySet(), 0, time))

    while (queue.isNotEmpty()) {
        val elem = queue.removeFirst()
        if (elem.time >= 1 && !elem.currentValveIsOpen()) {
            addToQueue(elem.openValve())
        }

        relevantDistances.getValue(elem.currentValve).forEach { (neighbour, distance) ->
            if (distance <= elem.time) {
                addToQueue(elem.move(neighbour, distance))
            }
        }
    }
    return best
}

fun main() {
    val input = readResourceAsString("/day16.txt")

    val valves = input.parseLines { parseValve(it) }

    var part1Result = 0
    part1 {
        part1Result = solve(valves, 30).values.max()
        part1Result
    }

    part2 {
        val endStates = solve(valves, 26).filter { it.key.time == 1 }

        endStates
            .asSequence()
            .flatMap { a -> endStates.map { b -> a to b } }
            .filter { (a, b) -> a.value + b.value > part1Result }
            .filter { (a, b) -> a.key.openValves.intersect(b.key.openValves).isEmpty() }
            .maxBy { (a, b) -> a.value + b.value }
            .let { (a, b) -> a.value + b.value }
    }
}
