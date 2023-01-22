import Color.BLACK
import Color.TRANSPARENT
import Color.WHITE
import utils.BLOCK
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.wrap

private enum class Color {
    BLACK,
    WHITE,
    TRANSPARENT
}

private fun parseColor(value: Int) =
    when (value) {
        0 -> BLACK
        1 -> WHITE
        2 -> TRANSPARENT
        else -> error("Unknown color: $value")
    }

private class DSNImage(val data: List<Color>, val width: Int, val height: Int) {
    val layerSize = width * height
    val numLayers = data.size / layerSize
    val layers = (0 until numLayers).map { getLayer(it) }

    private fun getColor(layer: Int, x: Int, y: Int) = layers[layer][y * width + x]

    private fun getLayer(index: Int) =
        data.subList(index * layerSize, (index * layerSize) + layerSize)

    fun fullImage(): List<Color> {
        return (0 until height).flatMap { y ->
            (0 until width).map { x ->
                val colors = (0 until numLayers).map { getColor(it, x, y) }

                colors.first { it != TRANSPARENT }
            }
        }
    }
}

fun main() {
    val input = readResourceAsString("/day08.txt")
    val data = input.trim().map { parseColor(it.digitToInt()) }
    val image = DSNImage(data, 25, 6)

    part1 {
        val minLayer = image.layers.minBy { l -> l.count { it == BLACK } }

        minLayer.count { it == WHITE } * minLayer.count { it == TRANSPARENT }
    }
    part2 {
        image
            .fullImage()
            .mapIndexed { index, color ->
                val maybeNewLine = if ((index + 1) % image.width == 0) "\n" else ""

                when (color) {
                    WHITE -> "${BLOCK}$maybeNewLine"
                    BLACK -> " $maybeNewLine"
                    else -> error("Did not expect transparent pixel")
                }
            }
            .joinToString("")
            .wrap("\n")
    }
}
