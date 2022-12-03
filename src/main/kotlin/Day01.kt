fun main() {
    val fileContents = readResourceAsString("day01.txt")

    val calories =
        fileContents
            .split("\n\n")
            .map { it.split("\n").filterNotBlank().mapInts().sum() }
            .sortedDescending()

    part1 {
        calories.first()
    }

    part2 {
        calories.subList(0, 3).sum()
    }
}
