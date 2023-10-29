import DirectoryEntry.EntryType.DIR
import datastructures.Tree
import utils.filterNotBlank
import utils.firstRest
import utils.match
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toPair
import utils.transform

sealed interface Command {
    companion object {
        fun parse(input: String): Command {
            return when {
                input == "ls" -> ListDir
                input.startsWith("cd") -> {
                    val (target) = input.match("""cd (.+)""")
                    if (target == "..") {
                        ChangeDirUp
                    } else {
                        ChangeDir(target)
                    }
                }
                else -> error("Unknown command: $input")
            }
        }
    }

    fun parseOutput(input: List<String>): Output = Output.None

    data class ChangeDir(val to: String) : Command

    object ChangeDirUp : Command

    object ListDir : Command {
        override fun parseOutput(input: List<String>): Output.DirectoryList {
            val entries = input.map { DirectoryEntry.parse(it) }

            return Output.DirectoryList(entries)
        }
    }
}

data class DirectoryEntry(val type: EntryType, val size: Int, val name: String) {
    enum class EntryType {
        DIR,
        FILE
    }

    companion object {
        fun parse(input: String): DirectoryEntry {
            val (sizeOrDir, name) = input.match("""(\d+|dir) (.+)""").toPair()

            if (sizeOrDir == "dir") {
                return DirectoryEntry(DIR, 0, name)
            }

            return DirectoryEntry(EntryType.FILE, sizeOrDir.toInt(), name)
        }
    }
}

sealed interface Output {
    object None : Output

    data class DirectoryList(val entries: List<DirectoryEntry>) : Output
}

data class CommandHistory(val command: Command, val output: Output) {
    companion object {
        fun parse(input: String): CommandHistory {
            val (command, output) =
                input.lines().firstRest().transform { (command, output) ->
                    val cmd = Command.parse(command)

                    Pair(cmd, cmd.parseOutput(output))
                }

            return CommandHistory(command, output)
        }
    }
}

fun parseInput(input: String): List<CommandHistory> =
    input.split("$").filterNotBlank().map { CommandHistory.parse(it) }

fun reconstructFilesystem(input: List<CommandHistory>): Tree<DirectoryEntry> {
    val fsTree = Tree<DirectoryEntry>()

    var currentNode = fsTree.addRoot(id = null, DirectoryEntry(DIR, 0, "/"))

    input.forEach { entry ->
        val cmd = entry.command
        when {
            cmd is Command.ChangeDir && cmd.to == "/" -> currentNode = fsTree.root
            cmd is Command.ChangeDir -> {
                val child =
                    currentNode.findChild { it.name == cmd.to }
                        ?: error("Could not find child ${cmd.to} in $currentNode")

                currentNode = child
            }
            cmd is Command.ChangeDirUp -> currentNode = currentNode.parent!!
            cmd is Command.ListDir -> {
                val dirs = entry.output as Output.DirectoryList
                dirs.entries.forEach { currentNode.addChild(id = null, it) }
            }
        }
    }

    return fsTree
}

fun determineDirSizes(input: String): Map<String, Long> {
    val fsTree = reconstructFilesystem(parseInput(input))

    fun determineSize(node: Tree<DirectoryEntry>.Node): Long {
        val size =
            if (node.data.type == DirectoryEntry.EntryType.FILE) {
                node.data.size.toLong()
            } else {
                node.children.sumOf { determineSize(it) }
            }

        return size
    }

    return fsTree.allNodes().filter { it.data.type == DIR }.associate { it.id to determineSize(it) }
}

fun main() {
    val input = readResourceAsString("/day07.txt")

    part1 { determineDirSizes(input).values.filter { it <= 100000 }.sum() }
    part2 {
        val sizes = determineDirSizes(input)
        val totalSize = sizes.getValue("0")
        val unused = 70000000 - totalSize

        sizes.values.filter { it + unused > 30000000 }.min()
    }
}
