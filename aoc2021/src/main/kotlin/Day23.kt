import AmphipodType.AMBER
import AmphipodType.BRONZE
import AmphipodType.COPPER
import AmphipodType.DESERT
import algorithms.astar
import kotlin.math.absoluteValue
import utils.Scored
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.transpose
import utils.updated

private enum class AmphipodType(val energy: Int, val targetRoom: Int) {
    AMBER(1, 0),
    BRONZE(10, 1),
    COPPER(100, 2),
    DESERT(1000, 3)
}

private fun parseAmphipodType(char: Char) =
    when (char) {
        'A' -> AMBER
        'B' -> BRONZE
        'C' -> COPPER
        'D' -> DESERT
        else -> error("Unknown type: $char")
    }

private class Amphipod(val type: AmphipodType) {
    val energy
        get() = type.energy
    val targetRoom
        get() = type.targetRoom

    override fun toString(): String {
        return "Amphipod(type=$type,id=${super.toString()})"
    }
}

private sealed interface Move {
    val distance: Int

    data class ToHallway(
        val amphipod: Amphipod,
        val hallwayIndex: Int,
        override val distance: Int
    ) : Move

    data class ToRoom(val amphipod: Amphipod, val roomIndex: Int, override val distance: Int) :
        Move
}

private data class HallwayTile(val index: Int, val amphipod: Amphipod)

private data class Hallway(val tiles: List<HallwayTile>) {
    companion object {
        const val SIZE = 11
    }

    private val occupiedTiles by lazy { tiles.mapTo(HashSet(tiles.size)) { it.index } }

    fun amphipodAtIndex(index: Int): HallwayTile? = tiles.find { it.index == index }

    fun findAmphipod(amphipod: Amphipod) = tiles.find { it.amphipod == amphipod }

    fun removeAmphipod(amphipod: Amphipod) = copy(tiles = tiles.filter { it.amphipod != amphipod })

    fun addAmphipod(index: Int, amphipod: Amphipod) =
        copy(tiles = tiles + HallwayTile(index, amphipod))

    fun hasFreePath(fromIndex: Int, toIndex: Int): Boolean {
        val range = if (fromIndex < toIndex) fromIndex + 1..toIndex else toIndex until fromIndex

        return occupiedTiles.all { it !in range }
    }

    fun reachableTiles(fromIndex: Int): List<Int> {
        val result = mutableListOf<Int>()

        val occupiedTiles = occupiedTiles

        var leftCursor = fromIndex
        var leftBoundFound = false

        var rightCursor = fromIndex
        var rightBoundFound = false
        while (!leftBoundFound || !rightBoundFound) {
            if (!leftBoundFound) {
                val left = leftCursor - 1
                if (left < 0 || left in occupiedTiles) {
                    leftBoundFound = true
                } else {
                    result.add(left)
                    leftCursor = left
                }
            }

            if (!rightBoundFound) {
                val right = rightCursor + 1
                if (right >= SIZE || right in occupiedTiles) {
                    rightBoundFound = true
                } else {
                    result.add(right)
                    rightCursor = right
                }
            }
        }

        return result
    }
}

private data class SideRoom(val index: Int, val size: Int, val amphipods: List<Amphipod>) {
    val freeSpace
        get() = size - amphipods.size

    val empty
        get() = amphipods.isEmpty()

    val full
        get() = freeSpace == 0

    val isComplete by lazy { full && amphipods.all { it.targetRoom == this.index } }

    val hallwayEntrance = (index + 1) * 2

    fun addAmphipod(amphipod: Amphipod) = copy(amphipods = listOf(amphipod) + amphipods)
    fun removeTopMostAmphipod() = copy(amphipods = amphipods.drop(1))
    fun topAmphipod() = amphipods.first()
    fun accepts(amphipod: Amphipod) = !full && hasOnlyAmphipodsOfType(amphipod.type)
    fun hasOnlyAmphipodsOfType(type: AmphipodType) = amphipods.all { it.type == type }

    operator fun contains(amphipod: Amphipod) = amphipod in amphipods
}

private data class Burrow(
    val energyUsed: Int,
    val hallway: Hallway,
    val sideRooms: List<SideRoom>,
) {
    val incompleteRooms by lazy { sideRooms.filter { !it.isComplete } }
    val hallwayEntrances by lazy { sideRooms.mapTo(HashSet()) { it.hallwayEntrance } }

    val complete
        get() = incompleteRooms.isEmpty()

    fun possibleMoves(): List<Move> {
        val movesFromRoom =
            incompleteRooms.flatMap { room ->
                if (room.empty) {
                    return@flatMap emptyList()
                }

                val moveToTarget = findMoveFromRoomToTargetRoom(room)
                if (moveToTarget.isNotEmpty()) {
                    return@flatMap moveToTarget
                }

                findMovesFromRoomToHallway(room)
            }

        val movesFromHallway =
            hallway.tiles.mapNotNull { (hallwayIndex, amphipod) ->
                val targetRoomIndex = amphipod.targetRoom
                val targetRoom = sideRooms[targetRoomIndex]

                if (!targetRoom.accepts(amphipod)) {
                    return@mapNotNull null
                }

                val canMoveOnHallway = hallway.hasFreePath(hallwayIndex, targetRoom.hallwayEntrance)
                if (!canMoveOnHallway) {
                    return@mapNotNull null
                }
                val distance = distanceFromHallwayToRoom(hallwayIndex, targetRoom)

                Move.ToRoom(amphipod, targetRoomIndex, distance * amphipod.type.energy)
            }

        return movesFromHallway + movesFromRoom
    }

    private fun findMovesFromRoomToHallway(room: SideRoom): List<Move> {
        val amphipod = room.topAmphipod()
        val spots = hallway.reachableTiles(room.hallwayEntrance)
        if (amphipod.targetRoom == room.index && room.hasOnlyAmphipodsOfType(amphipod.type)) {
            return emptyList()
        }

        return spots
            .filter { it !in hallwayEntrances }
            .map {
                val distance = distanceFromRoomToHallway(room, it)

                Move.ToHallway(amphipod, it, distance * amphipod.energy)
            }
    }

    private fun findMoveFromRoomToTargetRoom(room: SideRoom): List<Move.ToRoom> {
        val amphipod = room.topAmphipod()
        val targetRoom = sideRooms[amphipod.targetRoom]
        if (targetRoom.index == room.index) {
            return emptyList()
        }
        if (!targetRoom.accepts(amphipod)) {
            return emptyList()
        }

        if (!hallway.hasFreePath(room.hallwayEntrance, targetRoom.hallwayEntrance)) {
            return emptyList()
        }

        val distance = distanceFromRoomToRoom(room, targetRoom)
        return listOf(Move.ToRoom(amphipod, targetRoom.index, distance * amphipod.energy))
    }

    fun moveToHallway(amphipod: Amphipod, hallwayId: Int): Burrow {
        val (sideRoomIndex, _) =
            findSideRoomWithAmphipod(amphipod) ?: error("Could not find amphipod to move")
        val sideRoom = sideRooms[sideRoomIndex]

        val newSideRoom = sideRoom.removeTopMostAmphipod()
        val newSideRooms = sideRooms.updated(sideRoomIndex, newSideRoom)
        val newHallway = hallway.addAmphipod(hallwayId, amphipod)

        val distance = distanceFromRoomToHallway(sideRoom, hallwayId)
        return copy(
            energyUsed = energyUsed + distance * amphipod.type.energy,
            hallway = newHallway,
            sideRooms = newSideRooms)
    }

    fun score(): Int {
        val energyForAmphipodsInRooms =
            sideRooms.sumOf { room ->
                room.amphipods.sumOf { amphipod ->
                    if (amphipod.targetRoom == room.index) {
                        return 0
                    } else {
                        val target = sideRooms[amphipod.targetRoom]
                        distanceFromRoomToRoom(room, target) * amphipod.energy
                    }
                }
            }

        val energyForAmphipodsInHallway =
            hallway.tiles.sumOf { tile ->
                val target = sideRooms[tile.amphipod.targetRoom]
                distanceFromHallwayToRoom(tile.index, target) * tile.amphipod.energy
            }

        return energyForAmphipodsInRooms + energyForAmphipodsInHallway
    }

    private fun distanceFromRoomToHallway(fromRoom: SideRoom, hallwayIndex: Int): Int {
        val distanceFromRoomToHallway = fromRoom.freeSpace + 1
        val distanceInHallway = (fromRoom.hallwayEntrance - hallwayIndex).absoluteValue

        return distanceFromRoomToHallway + distanceInHallway
    }

    private fun distanceFromRoomToRoom(fromRoom: SideRoom, toRoom: SideRoom): Int {
        val distanceFromRoomToHallway = fromRoom.freeSpace + 1
        val distanceInHallway = (fromRoom.hallwayEntrance - toRoom.hallwayEntrance).absoluteValue
        val distanceFromHallwayToRoom = toRoom.freeSpace

        return distanceFromRoomToHallway + distanceInHallway + distanceFromHallwayToRoom
    }

    private fun distanceFromHallwayToRoom(amphipodInHallway: Int, toRoom: SideRoom): Int {
        val distanceInHallway = (amphipodInHallway - toRoom.hallwayEntrance).absoluteValue
        val distanceFromHallwayToRoom = toRoom.freeSpace

        return distanceInHallway + distanceFromHallwayToRoom
    }

    fun executeMove(move: Move) =
        when (move) {
            is Move.ToHallway -> moveToHallway(move.amphipod, move.hallwayIndex)
            is Move.ToRoom -> moveToRoom(move.amphipod, move.roomIndex)
        }

    fun moveToRoom(amphipod: Amphipod, toRoomIndex: Int): Burrow {
        val toSideRoom = sideRooms[toRoomIndex]
        val newToSideRoom = toSideRoom.addAmphipod(amphipod)

        val sideRoomWithAmphipod = findSideRoomWithAmphipod(amphipod)
        if (sideRoomWithAmphipod != null) {
            val newFromSideRoom = sideRoomWithAmphipod.removeTopMostAmphipod()

            val newSideRooms =
                sideRooms
                    .updated(sideRoomWithAmphipod.index, newFromSideRoom)
                    .updated(toRoomIndex, newToSideRoom)

            val distance = distanceFromRoomToRoom(sideRoomWithAmphipod, toSideRoom)

            return copy(
                energyUsed = energyUsed + distance * amphipod.type.energy,
                hallway = hallway,
                sideRooms = newSideRooms)
        }

        val amphipodInHallway = hallway.findAmphipod(amphipod)?.index
        if (amphipodInHallway != null) {
            val newHallway = hallway.removeAmphipod(amphipod)
            val newSideRooms = sideRooms.updated(toRoomIndex, newToSideRoom)

            val distance = distanceFromHallwayToRoom(amphipodInHallway, toSideRoom)
            return copy(
                energyUsed = energyUsed + distance * amphipod.type.energy,
                hallway = newHallway,
                sideRooms = newSideRooms)
        }

        error("Could not find amphipod to move")
    }

    private fun findSideRoomWithAmphipod(amphipod: Amphipod) = sideRooms.find { amphipod in it }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Burrow

        if (this.hallway != other.hallway) return false
        if (this.sideRooms != other.sideRooms) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hallway.hashCode()
        result = 31 * result + sideRooms.hashCode()
        return result
    }
}

private fun parseBurrow(input: String): Burrow {
    val lines = input.lines()
    val hallway = Hallway(emptyList())

    val sideRooms =
        lines
            .drop(2)
            .dropLast(1)
            .map { line -> line.filter { it.isLetter() }.map { Amphipod(parseAmphipodType(it)) } }
            .transpose()
            .mapIndexed { idx, amphipods -> SideRoom(idx, amphipods.size, amphipods) }

    return Burrow(0, hallway, sideRooms)
}

private fun solve(burrow: Burrow) =
    astar<Burrow> {
        start = burrow

        endCondition { it.complete }

        heuristic { it.score() }

        neighbours { current ->
            val possibleMoves = current.possibleMoves()

            possibleMoves.map { Scored(it.distance, current.executeMove(it)) }
        }
    }

fun main() {
    val input = readResourceAsString("/day23.txt")
    val burrow = parseBurrow(input)

    part1 { solve(burrow)?.energyUsed }
    part2 {
        val toInject = listOf("  #D#C#B#A#", "  #D#B#A#C#")
        val adjustedInput =
            (input.lines().slice((0..2)) +
                    toInject +
                    input.lines().slice(3 until input.lines().size))
                .joinToString("\n")

        solve(parseBurrow(adjustedInput))?.energyUsed
    }
}
