import java.util.BitSet
import utils.cartesian
import utils.clearAll
import utils.copy
import utils.map
import utils.match
import utils.parseLines
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.setAll
import utils.singleMatch
import utils.toEnum
import utils.toIntSet
import utils.toPair

private enum class ReactorLightState {
    ON,
    OFF;

    fun combine(other: ReactorLightState) =
        when {
            this == ON && other == OFF -> OFF
            this == ON && other == ON -> ON
            this == OFF && other == ON -> ON
            this == OFF && other == OFF -> OFF
            else -> error("Unreachable")
        }
}

private data class ReactorInstruction(
    val type: ReactorLightState,
    val xRange: IntRange,
    val yRange: IntRange,
    val zRange: IntRange
)

private fun parseReactorInstruction(input: String): ReactorInstruction {
    val type = input.match("(on|off)").singleMatch().toEnum<ReactorLightState>()

    return ReactorInstruction(
        type, parseRange("x", input), parseRange("y", input), parseRange("z", input))
}

private fun parseRange(dimension: String, input: String): IntRange =
    input
        .match("""$dimension=(-?\d+)..(-?\d+)""")
        .toPair()
        .map { it.toInt() }
        .let { (a, b) -> a..b }

private enum class CheckpointType {
    START,
    END
}

private data class Checkpoint(val originalRangeId: Int, val point: Int, val type: CheckpointType)

private data class SubdividedRange(val originalRangeIds: BitSet, val range: IntRange)

private data class SubdividedBox(
    val originalRangeIds: Set<Int>,
    val xRange: IntRange,
    val yRange: IntRange,
    val zRange: IntRange
)

fun isRangeInBounds(range: IntRange, lowerBound: Int, upperBound: Int) =
    range.first > lowerBound && range.last < upperBound

private fun subdivideBoxes(instructions: List<ReactorInstruction>): Sequence<SubdividedBox> {
    val xRanges = subdivideRanges(instructions.map { it.xRange })
    val yRanges = subdivideRanges(instructions.map { it.yRange })
    val zRanges = subdivideRanges(instructions.map { it.zRange })

    return xRanges.cartesian(yRanges).cartesian(zRanges).mapNotNull { (p, z) ->
        val (x, y) = p

        val xRangeIds = x.originalRangeIds
        val yRangeIds = y.originalRangeIds
        val zRangeIds = z.originalRangeIds

        val intersection = xRangeIds.copy()
        intersection.and(yRangeIds)
        intersection.and(zRangeIds)

        if (!intersection.isEmpty) {
            SubdividedBox(intersection.toIntSet(), x.range, y.range, z.range)
        } else {
            null
        }
    }
}

private fun subdivideRanges(ranges: List<IntRange>): List<SubdividedRange> {
    val checkpoints =
        ranges
            .flatMapIndexed { i, range ->
                listOf(
                    Checkpoint(i, range.first, CheckpointType.START),
                    Checkpoint(i, range.last + 1, CheckpointType.END))
            }
            .groupBy { it.point }

    fun getOriginalRanges(point: Int, type: CheckpointType) =
        checkpoints.getValue(point).filter { it.type == type }.map { it.originalRangeId }

    val allPoints = checkpoints.keys.sorted()

    val activeRanges = BitSet()
    var lastPoint = allPoints.first()

    activeRanges.setAll(getOriginalRanges(lastPoint, CheckpointType.START))
    val result = mutableListOf<SubdividedRange>()
    for (currentPoint in allPoints.drop(1)) {
        result += SubdividedRange(activeRanges.copy(), lastPoint..currentPoint)

        activeRanges.setAll(getOriginalRanges(currentPoint, CheckpointType.START))
        activeRanges.clearAll(getOriginalRanges(currentPoint, CheckpointType.END))

        lastPoint = currentPoint
    }

    return result
}

private fun calculateResult(
    subdividedBoxes: Sequence<SubdividedBox>,
    instructions: List<ReactorInstruction>
) =
    subdividedBoxes.sumOf { box ->
        val rangeLightState =
            box.originalRangeIds.sorted().fold(ReactorLightState.OFF) { acc, idx ->
                acc.combine(instructions[idx].type)
            }

        if (rangeLightState == ReactorLightState.ON) {
            (box.xRange.last.toLong() - box.xRange.first.toLong()) *
                (box.yRange.last.toLong() - box.yRange.first.toLong()) *
                (box.zRange.last.toLong() - box.zRange.first.toLong())
        } else {
            0
        }
    }

fun main() {
    val input = readResourceAsString("/day22.txt")
    val instructions = input.parseLines { parseReactorInstruction(it) }

    part1 {
        val reducedInstructions =
            instructions.filter {
                isRangeInBounds(it.xRange, -500, 500) &&
                    isRangeInBounds(it.yRange, -500, 500) &&
                    isRangeInBounds(it.zRange, -500, 500)
            }

        calculateResult(subdivideBoxes(reducedInstructions), instructions)
    }

    part2 { calculateResult(subdivideBoxes(instructions), instructions) }
}
