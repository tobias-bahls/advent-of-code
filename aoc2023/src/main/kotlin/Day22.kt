import algorithms.repeat
import datastructures.Point3D
import datastructures.parsePoint3D
import utils.*

private data class Brick(val id: String, val from: Point3D, val to: Point3D) {
    val elems =
        (from.x..to.x)
            .flatMap { x ->
                (from.y..to.y).flatMap { y -> (from.z..to.z).map { z -> Point3D(x, y, z) } }
            }
            .toSet()

    fun moveDown() = Brick(id = id, from + Point3D(0, 0, -1), to + Point3D(0, 0, -1))
}

private fun parseBrick(id: String, str: String) =
    str.split("~").map { parsePoint3D(it) }.let { (from, to) -> Brick(id, from, to) }

private fun settle(initialBricks: List<Brick>): List<Brick> {
    data class State(val bricks: List<Brick>, val blocked: Map<Point3D, Brick>)

    val initialBlocked = initialBricks.flatMap { brick -> brick.elems.map { it to brick } }.toMap()
    val initialState = State(initialBricks, initialBlocked)

    val settled =
        repeat(initialState) { state ->
                val (toMove, static) =
                    state.bricks
                        .partition { brick ->
                            val candidate = brick.moveDown()

                            candidate.elems.all { candidatePoint ->
                                val blockedBy = state.blocked[candidatePoint]

                                candidatePoint.z > 0 && (blockedBy == null || blockedBy == brick)
                            }
                        }
                        .map { it.toSet() }

                if (toMove.isEmpty()) {
                    return@repeat stop()
                }

                val moved = toMove.map { it.moveDown() }
                val nextBricks = (moved + static)
                val nextBlocked =
                    state.blocked.filterValues { it !in toMove } +
                        moved.flatMap { brick -> brick.elems.map { it to brick } }.toMap()

                next(State(nextBricks, nextBlocked))
            }
            .element

    return settled.bricks
}

private fun buildSupportMap(settled: List<Brick>): Map<Brick, List<Brick>> =
    settled.associateWith { brick ->
        val supportPoints = brick.elems.map { it + Point3D(0, 0, 1) }
        val supported =
            settled.filter { otherBrick ->
                otherBrick != brick && otherBrick.elems.any { it in supportPoints }
            }

        supported
    }

fun main() {
    part1 {
        val input = readResourceAsString("/day22.txt")

        val initialBricks =
            input.lines().filterNotBlank().mapIndexed { index, s ->
                parseBrick(index.toString(), s)
            }
        val settled = settle(initialBricks)

        val supportMap = buildSupportMap(settled)

        settled.count { brick ->
            val supports = supportMap[brick] ?: emptyList()

            supports.all { supportedBrick ->
                val otherSupports = supportMap.filterValues { supportedBrick in it }

                otherSupports.size > 1
            }
        }
    }

    part2 {
        val input = readResourceAsString("/day22.txt")

        val initialBricks =
            input.lines().filterNotBlank().mapIndexed { index, s ->
                parseBrick(index.toString(), s)
            }
        val settled = settle(initialBricks).sortedBy { it.id }

        val supportMap = buildSupportMap(settled)

        fun solve(unsupported: List<Brick>, remaining: Map<Brick, List<Brick>>): Set<Brick> {
            val removedSupport = remaining.filterKeys { it !in unsupported }
            if (removedSupport.isEmpty()) {
                return emptySet()
            }
            if (unsupported.none { it in remaining.keys }) {
                return emptySet()
            }

            val supportedBricks = removedSupport.values.flatten().toSet()

            val unsupportedNow =
                settled
                    .filter { other -> other !in supportedBricks && other.elems.none { it.z == 1 } }
                    .toSet()
            if (unsupportedNow.isEmpty()) {
                return emptySet()
            }

            val result = unsupportedNow + solve(unsupported + unsupportedNow, removedSupport)
            return result
        }

        settled.mapAsync { solve(listOf(it), supportMap).size }.awaitAll().sum()
    }
}
