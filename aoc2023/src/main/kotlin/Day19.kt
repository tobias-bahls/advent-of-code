import CompareCondition.Operation.*
import CompareCondition.Property.*
import algorithms.repeat
import utils.*

private data class MachinePart(
    val xtremelyCoolLooking: Int,
    val musical: Int,
    val aerodynamic: Int,
    val shiny: Int
) {
    val score = xtremelyCoolLooking + musical + aerodynamic + shiny
}

private fun parseMachinePart(str: String) =
    str.match("\\{x=(\\d+),m=(\\d+),a=(\\d+),s=(\\d+)}")
        .toList()
        .map { it.toInt() }
        .let { (x, m, a, s) -> MachinePart(x, m, a, s) }

private sealed interface Day19Condition {
    fun evaluate(part: MachinePart): Boolean

    fun inverse(): Day19Condition
}

private data class CompareCondition(val property: Property, val test: Int, val op: Operation) :
    Day19Condition {
    enum class Property {
        XTREMELY_COOL_LOOKING,
        MUSICAL,
        AERODYNAMIC,
        SHINY
    }

    enum class Operation {
        LT,
        GT,
    }

    override fun evaluate(part: MachinePart): Boolean {
        val actual =
            when (property) {
                XTREMELY_COOL_LOOKING -> part.xtremelyCoolLooking
                MUSICAL -> part.musical
                AERODYNAMIC -> part.aerodynamic
                SHINY -> part.shiny
            }

        return when (op) {
            LT -> actual < test
            GT -> actual > test
        }
    }

    override fun inverse(): Day19Condition =
        when (op) {
            LT -> copy(op = GT, test = test - 1)
            GT -> copy(op = LT, test = test + 1)
        }
}

private data object NoOpCondition : Day19Condition {
    override fun evaluate(part: MachinePart) = true

    override fun inverse(): Day19Condition = this
}

private data class Day19Rule(val condition: Day19Condition, val target: String) {
    fun evaluate(machinePart: MachinePart) = condition.evaluate(machinePart)

    fun invertCondition() = copy(condition = condition.inverse())
}

private fun parseRule(str: String) =
    if (':' in str) {
        val (condition, target) = str.split(":")
        val (prop, op, test) = condition.match("([a-z])([<>])(\\d+)")

        Day19Rule(
            condition =
                CompareCondition(
                    property =
                        when (prop) {
                            "x" -> XTREMELY_COOL_LOOKING
                            "m" -> MUSICAL
                            "a" -> AERODYNAMIC
                            "s" -> SHINY
                            else -> unreachable("property $prop")
                        },
                    op =
                        when (op) {
                            ">" -> GT
                            "<" -> LT
                            else -> unreachable("op $op")
                        },
                    test = test.toInt()),
            target = target)
    } else {
        Day19Rule(NoOpCondition, str)
    }

private data class Day19Workflow(val id: String, val rules: List<Day19Rule>) {
    fun evaluate(machinePart: MachinePart) = rules.first { it.evaluate(machinePart) }.target
}

private enum class Day19WorkflowResult {
    ACCEPT,
    REJECT,
}

private data class Day19Workflows(val workflows: List<Day19Workflow>) {
    val workflowsById = workflows.associateBy { it.id }

    fun evaluate(machinePart: MachinePart): Day19WorkflowResult {
        val result =
            repeat("in") { currentWorkflowId ->
                if (currentWorkflowId == "A" || currentWorkflowId == "R") {
                    return@repeat stop()
                }

                val workflow =
                    workflowsById[currentWorkflowId]
                        ?: error("no workflow with id $currentWorkflowId")
                next(workflow.evaluate(machinePart))
            }

        return when (result.element) {
            "A" -> Day19WorkflowResult.ACCEPT
            "R" -> Day19WorkflowResult.REJECT
            else -> unreachable("unknown result: $result")
        }
    }
}

private fun parseWorkflow(str: String) =
    str.match("(.*)\\{(.*)}").let { (id, rulesStr) ->
        Day19Workflow(id = id, rules = rulesStr.split(",").map { parseRule(it) })
    }

fun main() {
    part1 {
        val input = readResourceAsString("/day19.txt")
        val (workflowStr, machinePartsStr) = input.twoParts("\n\n")

        val machineParts = machinePartsStr.parseLines { parseMachinePart(it) }
        val workflows = Day19Workflows(workflowStr.parseLines { parseWorkflow(it) })

        machineParts
            .filter { workflows.evaluate(it) == Day19WorkflowResult.ACCEPT }
            .sumOf { it.score }
    }

    part2 {
        val input = readResourceAsString("/day19.txt")
        val workflowStr = input.twoParts("\n\n").first
        val workflows = workflowStr.parseLines { parseWorkflow(it) }
        val workflowsById = workflows.associateBy { it.id }

        fun calculatePaths(path: List<Day19Rule>): List<List<Day19Rule>> {
            val last = path.last().target
            if (last == "A" || last == "R") {
                return listOf(path)
            }
            val lastWorkflow = workflowsById[last]!!

            return lastWorkflow.rules.flatMapIndexed { idx, rule ->
                val invertedConditionsUntilHere =
                    lastWorkflow.rules.subList(0, idx).map { it.invertCondition() }
                calculatePaths(path + invertedConditionsUntilHere + rule)
            }
        }

        val paths = calculatePaths(listOf(Day19Rule(NoOpCondition, "in")))

        paths
            .filter { it.last().target == "A" }
            .sumOf { path ->
                val ranges =
                    mutableMapOf(
                        XTREMELY_COOL_LOOKING to (1..4000),
                        MUSICAL to (1..4000),
                        AERODYNAMIC to (1..4000),
                        SHINY to (1..4000))

                path.forEach { rule ->
                    val condition = rule.condition
                    if (condition !is CompareCondition) {
                        return@forEach
                    }
                    val currentRange = ranges[condition.property]!!
                    ranges[condition.property] =
                        when (condition.op) {
                            LT -> (currentRange.first ..< condition.test)
                            GT -> (condition.test + 1..currentRange.last)
                        }
                }

                ranges.values.map { ((it.last - it.first) + 1).toLong() }.reduce(Long::times)
            }
    }
}
