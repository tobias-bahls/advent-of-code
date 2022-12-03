import Resource.readResourceAsString

@JvmInline
value class Item(private val char: Char) {
  fun score(): Int {
    return when {
      char.isUpperCase() -> char.code - 65 + 27
      char.isLowerCase() -> char.code - 97 + 1
      else -> error("Char neither lower nor upper case: $char")
    }
  }
}

data class Rucksack(val left: Set<Item>, val right: Set<Item>) {
  companion object {
    fun fromString(string: String): Rucksack {
      val left = string.substring(0, string.length / 2).map { Item(it) }.toSet()
      val right = string.substring(string.length / 2, string.length).map { Item(it) }.toSet()

      return Rucksack(left, right)
    }
  }

  fun duplicates(): Set<Item> = left.intersect(right)

  fun items(): Set<Item> = left + right
}

data class Group(val rucksacks: List<Rucksack>) {
  init {
    check(rucksacks.size == 3)
  }

  fun badge(): Item =
      rucksacks
          .map(Rucksack::items)
          .fold(rucksacks.first().items()) { acc, items -> items.intersect(acc) }
          .first()
}

fun main() {
  val fileContents = readResourceAsString("day03.txt")

  val part1 =
      fileContents
          .lines()
          .filterNotBlank()
          .map { Rucksack.fromString(it) }
          .flatMap { it.duplicates().map(Item::score) }
          .sum()

  val part2 =
      fileContents
          .lines()
          .filterNotBlank()
          .map { Rucksack.fromString(it) }
          .windowed(3, 3)
          .sumOf { Group(it).badge().score() }

  println("First Part: $part1")
  println("Second Part: $part2")
}
