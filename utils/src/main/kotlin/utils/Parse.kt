package utils

fun <T> parseGrid(input: String, createTile: (x: Int, y: Int, char: Char) -> T): List<T> =
    input.lines().filterNotBlank().flatMapIndexed { y, row ->
        row.toCharArray().mapIndexed { x, char -> createTile(x, y, char) }
    }
