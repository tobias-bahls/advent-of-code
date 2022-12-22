package utils

import datastructures.Grid
import datastructures.Point2D
import datastructures.Tile

fun <T> parseGrid(input: String, createTile: (x: Int, y: Int, char: Char) -> T): List<T> =
    input.lines().filterNotBlank().flatMapIndexed { y, row ->
        row.toCharArray().mapIndexed { x, char -> createTile(x, y, char) }
    }

fun <T> parseGridWithEmptyTiles(input: String, tileData: (char: Char) -> T): Grid<T> {
    val tiles =
        input.lines().flatMapIndexed { y, row ->
            row.toCharArray()
                .mapIndexed { x, char ->
                    if (char == ' ') return@mapIndexed null

                    Tile(Point2D(x, y), tileData(char))
                }
                .filterNotNull()
        }

    return Grid(tiles)
}
