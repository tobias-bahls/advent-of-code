import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import utils.*

fun main() {
    val input = readResourceAsString("/day04.txt")

    val parsed = input.parseLines { parseRecord(it) }
    part1 {
        val days = reconstructDays(parsed)

        val mostAsleepRobot =
            days
                .groupingBy { it.robotOnDuty }
                .fold(0) { acc, elem -> acc + elem.sleepTimes.sumOf { it.toList().size } }
                .maxBy { it.value }
                .key

        val robotDays = days.filter { it.robotOnDuty == mostAsleepRobot }
        val asleepCounter = Counter<Int>()
        robotDays.forEach { day -> asleepCounter.increment(day.sleepTimes.flatMap { it.toList() }) }

        mostAsleepRobot * asleepCounter.max()
    }

    part2 {
        val days = reconstructDays(parsed)

        val (robot, asleepMinuteCounter) =
            days
                .groupBy { it.robotOnDuty }
                .mapValues { (_, robotDays) ->
                    val asleepCounter = Counter<Int>()
                    robotDays.forEach { day ->
                        asleepCounter.increment(day.sleepTimes.flatMap { it.toList() })
                    }

                    asleepCounter
                }
                .maxBy { it.value.max() }

        robot * asleepMinuteCounter.maxKey()!!
    }
}

private fun reconstructDays(records: List<Day04Record>): List<Day04Day> {
    val days =
        records
            .sortedBy { it.timestamp }
            .chunksDelimitedBy { it.event is BeginShift }
            .map { chunk ->
                val beginEvent = chunk.first()
                check(beginEvent.event is BeginShift)
                if (chunk.size == 1) {
                    return@map Day04Day(
                        beginEvent.timestamp.toLocalDate(), beginEvent.event.guardId, emptyList())
                }

                val firstSleepEvent = chunk[1]
                check(firstSleepEvent.event is FallAsleep)

                val sleepTimes =
                    chunk.drop(1).windowed(2, 2).map { (sleep, wake) ->
                        check(sleep.event is FallAsleep)
                        check(wake.event is WakeUp)

                        val sleepMinute = sleep.timestamp.minute
                        val awakeMinute = wake.timestamp.minute

                        (sleepMinute until awakeMinute)
                    }

                Day04Day(
                    day = firstSleepEvent.timestamp.toLocalDate(),
                    robotOnDuty = beginEvent.event.guardId,
                    sleepTimes = sleepTimes)
            }
            .toList()
    return days
}

private data class Day04Day(
    val day: LocalDate,
    val robotOnDuty: Int,
    val sleepTimes: List<IntRange>
)

private sealed interface Day04Event

data object WakeUp : Day04Event

data object FallAsleep : Day04Event

data class BeginShift(val guardId: Int) : Day04Event

private data class Day04Record(val timestamp: LocalDateTime, val event: Day04Event)

private val timestampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

private fun parseRecord(line: String): Day04Record {
    val (timestamp, rest) = line.match("\\[(.+?)] (.+)")

    val event =
        when {
            rest.contains("wakes up") -> WakeUp
            rest.contains("falls asleep") -> FallAsleep
            rest.contains("begins shift") -> {
                val guardId = rest.match("Guard #(\\d+) begins shift").singleMatch().toInt()
                BeginShift(guardId)
            }
            else -> error("Could not parse line: '$rest'")
        }

    return Day04Record(timestamp = LocalDateTime.parse(timestamp, timestampFormat), event = event)
}
