import Resource.readResourceAsString

fun main() {
  val fileContents = readResourceAsString("day01.txt")

  val calories = fileContents
      .split("\n\n")
      .map {
          it.split("\n").filterNotBlank().mapInts().sum()
      }
      .sortedDescending()

    println("First Part: ${calories.first()}")
    println("Second Part: ${calories.subList(0, 3).sum()}")
}
