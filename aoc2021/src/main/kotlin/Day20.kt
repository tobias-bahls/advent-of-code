import datastructures.Grid
import datastructures.Point2D
import datastructures.Tile
import datastructures.parseGrid
import utils.cartesian
import utils.extend
import utils.interpretAsBinary
import utils.mapFirst
import utils.mapSecond
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.reduceTimes
import utils.twoParts

private data class PictureData(
    val grid: Grid<Boolean>,
    val algorithm: String,
    val infinityData: Boolean = false
) {
    val infinityFlips
        get() = algorithm[0] == '#'

    fun updatePicture(grid: Grid<Boolean>) =
        copy(
            grid = grid,
            infinityData =
                if (infinityFlips) {
                    !infinityData
                } else {
                    infinityData
                })

    fun litTiles() = grid.tiles.count { it.data }
}

fun main() {
    val input = readResourceAsString("/day20.txt")
    val (algorithm, picture) =
        input
            .twoParts("\n\n")
            .mapFirst { it.replace("\n", "") }
            .mapSecond { asciiPicture -> parseGrid(asciiPicture) { it == '#' } }

    part1 { reduceTimes(2, PictureData(picture, algorithm)) { enhancePicture(it) }.litTiles() }
    part2 { reduceTimes(50, PictureData(picture, algorithm)) { enhancePicture(it) }.litTiles() }
}

private fun enhancePicture(pictureData: PictureData): PictureData {
    val (picture, algorithm, emptyData) = pictureData

    val xRange = (0..picture.width).extend(2)
    val yRange = (0..picture.height).extend(2)
    val newTiles =
        xRange
            .cartesian(yRange)
            .map { (x, y) ->
                val point = Point2D(x, y)
                val fingerprint = getPixelFingerprint(point, picture, emptyData)
                if (algorithm[fingerprint] == '#') {
                    Tile(point + Point2D(2, 2), true)
                } else {
                    Tile(point + Point2D(2, 2), false)
                }
            }
            .toList()

    return pictureData.updatePicture(Grid(newTiles))
}

private fun getPixelFingerprint(
    centerPoint: Point2D,
    picture: Grid<Boolean>,
    emptyData: Boolean
): Int {
    val relevantPoints =
        listOf(
            centerPoint.topLeft,
            centerPoint.top,
            centerPoint.topRight,
            centerPoint.left,
            centerPoint,
            centerPoint.right,
            centerPoint.bottomLeft,
            centerPoint.bottom,
            centerPoint.bottomRight)

    return relevantPoints.map { picture.tileAt(it)?.data ?: emptyData }.interpretAsBinary()
}
