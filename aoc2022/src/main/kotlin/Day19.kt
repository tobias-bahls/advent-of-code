import utils.QueueMode
import utils.caseInsensitiveEnumValueOf
import utils.filterNotBlank
import utils.match
import utils.matchOrNull
import utils.parseLines
import utils.part1
import utils.part2
import utils.queue
import utils.readResourceAsString
import utils.singleMatch
import utils.singleMatchOrNull

private enum class Resource {
    ORE,
    CLAY,
    OBSIDIAN,
    GEODE
}

private fun parseResource(input: String) =
    caseInsensitiveEnumValueOf<Resource>(input) ?: error("Unknown resource: $input")

private data class Robot(
    val type: Resource,
    val oreCost: Int,
    val clayCost: Int,
    val obsidianCost: Int
)

private fun parseRobot(input: String): Robot {
    val robotType = input.match("""Each (\w+) robot""").singleMatch().let { parseResource(it) }
    val oreCost = input.match("""(\d+) ore""").singleMatch().toInt()
    val clayCost = input.matchOrNull("""(\d+) clay""").singleMatchOrNull()?.toInt() ?: 0
    val obsidianCost = input.matchOrNull("""(\d+) obsidian""").singleMatchOrNull()?.toInt() ?: 0

    return Robot(robotType, oreCost, clayCost, obsidianCost)
}

private data class Blueprint(val id: Int, val robots: List<Robot>) {
    fun forResource(resource: Resource) =
        robots.find { it.type == resource } ?: error("Could not find robot for $resource")

    val maxOreCost = robots.maxOf { it.oreCost }
    val maxClayCost = robots.maxOf { it.clayCost }
    val maxObsidianCost = robots.maxOf { it.obsidianCost }

    fun maxCost(resource: Resource) =
        when (resource) {
            Resource.ORE -> maxOreCost
            Resource.CLAY -> maxClayCost
            Resource.OBSIDIAN -> maxObsidianCost
            Resource.GEODE -> Integer.MIN_VALUE
        }
}

private fun parseBlueprint(input: String): Blueprint {
    val blueprintId = input.match("""Blueprint (\d+):""").singleMatch().toInt()

    val robots = input.split(".").filterNotBlank().map { parseRobot(it) }

    return Blueprint(blueprintId, robots)
}

private data class BlueprintState(
    val blueprint: Blueprint,
    val time: Int = 0,
    val oreRobots: Int = 1,
    val clayRobots: Int = 0,
    val obsidianRobots: Int = 0,
    val geodeRobots: Int = 0,
    val ore: Int = 0,
    val clay: Int = 0,
    val obsidian: Int = 0,
    val geodes: Int = 0,
    val toBuild: Resource? = null,
    val skippedBuilds: Set<Resource> = emptySet()
) {

    fun skipBuilding(possibilities: Set<Resource>) =
        copy(skippedBuilds = skippedBuilds + possibilities)

    fun tick() =
        copy(
            time = time + 1,
            ore = ore + oreRobots,
            clay = clay + clayRobots,
            obsidian = obsidian + obsidianRobots,
            geodes = geodes + geodeRobots,
            oreRobots = buildRobotIfQueued(Resource.ORE),
            clayRobots = buildRobotIfQueued(Resource.CLAY),
            obsidianRobots = buildRobotIfQueued(Resource.OBSIDIAN),
            geodeRobots = buildRobotIfQueued(Resource.GEODE),
            toBuild = null)

    fun nextBuilds(): Set<Resource> {
        val possible = possibleBuilds() - skippedBuilds

        if (Resource.GEODE in possible) {
            return setOf(Resource.GEODE)
        }

        return possible.filter { robotCountForResource(it) < blueprint.maxCost(it) }.toSet()
    }

    fun possibleBuilds() =
        Resource.values()
            .mapNotNull { resource ->
                val bot = blueprint.forResource(resource)

                if (ore >= bot.oreCost && clay >= bot.clayCost && obsidian >= bot.obsidianCost) {
                    resource
                } else {
                    null
                }
            }
            .toSet()

    fun enqueueBuild(resource: Resource): BlueprintState {
        val robot = blueprint.forResource(resource)

        return copy(
            toBuild = resource,
            ore = ore - robot.oreCost,
            clay = clay - robot.clayCost,
            obsidian = obsidian - robot.obsidianCost,
            skippedBuilds = emptySet())
    }

    fun buildRobotIfQueued(resource: Resource): Int {
        val toAdd =
            if (resource != toBuild) {
                0
            } else {
                1
            }

        return robotCountForResource(resource) + toAdd
    }

    fun robotCountForResource(resource: Resource) =
        when (resource) {
            Resource.ORE -> oreRobots
            Resource.CLAY -> clayRobots
            Resource.OBSIDIAN -> obsidianRobots
            Resource.GEODE -> geodeRobots
        }
}

fun main() {
    val input = readResourceAsString("/day19.txt")
    val blueprints = input.parseLines { parseBlueprint(it) }

    part1 { blueprints.sumOf { findBestEndState(it, 24).geodes * it.id } }
    part2 { blueprints.take(3).map { findBestEndState(it, 32).geodes }.reduce(Int::times) }
}

private fun findBestEndState(blueprint: Blueprint, minutes: Int): BlueprintState {
    val endStates = mutableSetOf<BlueprintState>()

    queue(BlueprintState(blueprint), QueueMode.LIFO) { currentState ->
        if (currentState.time == minutes) {
            endStates += currentState
            return@queue skip()
        }

        val possibleBuilds = currentState.nextBuilds()
        if (possibleBuilds.isEmpty()) {
            enqueue(currentState.tick())
        } else {
            val buildStates = possibleBuilds.map { currentState.enqueueBuild(it).tick() }
            enqueue(buildStates + currentState.skipBuilding(possibleBuilds).tick())
        }
    }

    return endStates.maxBy { it.geodes }
}
