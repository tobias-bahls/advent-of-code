import datastructures.parsePoint2D
import utils.parseLines
import utils.part1

fun main() {
    part1 {
        val sample =
            """
            1, 1
            1, 6
            8, 3
            3, 4
            5, 5
            8, 9
        """
                .trimIndent()

        val points = sample.parseLines { parsePoint2D(it) }

    }
}
