import datastructures.Point2D
import utils.cycle
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.zipWithIndex

enum class Jet {
    LEFT,
    RIGHT
}

val horizontalBarShape = listOf(Point2D(0, 0), Point2D(1, 0), Point2D(2, 0), Point2D(3, 0))
val plusShape = listOf(Point2D(1, 2), Point2D(0, 1), Point2D(1, 1), Point2D(2, 1), Point2D(1, 0))
val lShape = listOf(Point2D(2, 2), Point2D(2, 1), Point2D(0, 0), Point2D(1, 0), Point2D(2, 0))
val verticalBarShape = listOf(Point2D(0, 3), Point2D(0, 2), Point2D(0, 1), Point2D(0, 0))
val cubeShape = listOf(Point2D(0, 1), Point2D(1, 1), Point2D(0, 0), Point2D(1, 0))

private class Rock(var points: List<Point2D>) {
    val leftEdge
        get() = points.minOf { it.x }

    val rightEdge
        get() = points.maxOf { it.x }

    val bottomEdge
        get() = points.minOf { it.y }

    fun move(by: Point2D) {
        points = points.map { it + by }
    }

    fun moveLeft() {
        move(Point2D(-1, 0))
    }

    fun moveRight() {
        move(Point2D(1, 0))
    }

    fun moveDown() {
        move(Point2D(0, -1))
    }

    fun moveUp() {
        move(Point2D(0, 1))
    }

    override fun toString(): String {
        return "Rock(points=$points)"
    }
}

private class Chamber(jets: List<Jet>) {
    var occupiedPoints = mutableSetOf<Point2D>()
    val jets = jets.zipWithIndex().cycle().iterator()
    var lastJetIndex = -1

    val height
        get() = occupiedPoints.maxOf { it.y } + 1

    val xHeights
        get() =
            (0..6)
                .map { x -> occupiedPoints.filter { it.x == x }.maxOfOrNull { it.y } ?: 0 }
                .map { it - (height - 1) }

    fun spawnRock(shape: List<Point2D>) {
        val highestRock = occupiedPoints.maxOfOrNull { it.y }?.plus(1) ?: 0

        val spawnY = highestRock + 3
        val spawnX = 2

        val rock = Rock(shape)
        rock.move(Point2D(spawnX, spawnY))

        simulate(rock)

        occupiedPoints += rock.points
    }

    private fun simulate(rock: Rock) {
        while (true) {
            val jet = jets.next()
            lastJetIndex = jet.index

            when (jet.elem) {
                Jet.LEFT -> {
                    rock.moveLeft()
                    if (rock.points.any { it in occupiedPoints } || rock.leftEdge == -1) {
                        rock.moveRight()
                    }
                }
                Jet.RIGHT -> {
                    rock.moveRight()
                    if (rock.points.any { it in occupiedPoints } || rock.rightEdge > 6) {
                        rock.moveLeft()
                    }
                }
            }

            rock.moveDown()
            val collision = rock.points.any { it in occupiedPoints } || rock.bottomEdge == -1
            if (collision) {
                rock.moveUp()
                return
            }
        }
    }
}

private fun parseJets(input: String): List<Jet> =
    input.trim().map {
        when (it) {
            '>' -> Jet.RIGHT
            '<' -> Jet.LEFT
            else -> error("Unknown char: $it")
        }
    }

fun main() {
    val input = readResourceAsString("/day17.txt")
    val jets = parseJets(input)

    val shapes = listOf(horizontalBarShape, plusShape, lShape, verticalBarShape, cubeShape)

    part1 {
        val shapeCycle = shapes.cycle().iterator()
        val chamber = Chamber(jets)
        repeat(2022) { chamber.spawnRock(shapeCycle.next()) }

        chamber.height
    }

    part2 {
        val shapeCycle = shapes.zipWithIndex().cycle().iterator()
        val chamber = Chamber(jets)

        data class Key(val shapeIndex: Int, val jetStreamIndex: Int, val xHeights: List<Int>)
        val seen = mutableMapOf<Key, Pair<Long, Long>>()

        var iterations = 0L
        var totalHeight = 0L
        while (iterations < 1000000000000) {
            val shape = shapeCycle.next()
            chamber.spawnRock(shape.elem)

            val key = Key(shape.index, chamber.lastJetIndex, chamber.xHeights)
            val seenPair = seen[key]
            if (seenPair != null) {
                val (previousIterations, cycleHeightGain) = seenPair
                val cycleWidth = iterations - previousIterations

                val repeats = (1000000000000 - previousIterations) / cycleWidth - 1
                iterations += cycleWidth * repeats
                totalHeight += (chamber.height - cycleHeightGain) * repeats
            } else {
                seen[key] = (iterations to chamber.height.toLong())
            }

            iterations++
        }

        totalHeight + chamber.height
    }
}
