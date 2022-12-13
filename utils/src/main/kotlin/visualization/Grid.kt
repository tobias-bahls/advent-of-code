package visualization

import datastructures.Grid
import datastructures.Point2D
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JViewport
import javax.swing.SwingUtilities

fun <T> visualizeGrid(grid: Grid<T>, path: List<Point2D> = emptyList(), toChar: (T) -> Char) {
    SwingUtilities.invokeLater {
        val frame = createBasicFrame(1000, 1000)
        val drawableCanvas = DrawableCanvas(3000, 1000)
        val jScrollPane = JScrollPane(drawableCanvas)
        jScrollPane.viewport.scrollMode = JViewport.SIMPLE_SCROLL_MODE
        jScrollPane.preferredSize = Dimension(1000, 1000)
        frame.contentPane.add(jScrollPane, BorderLayout.CENTER)
        frame.pack()

        val xOffs = 10
        val yOffs = 10

        val box = 10

        grid.tiles.forEach {
            val tileChar = toChar(it.data)
            val (x, y) = it.point

            drawableCanvas.drawString(x * box + xOffs, y * box + yOffs, tileChar.toString())
        }

        path.windowed(2).forEach { (from, to) ->
            drawableCanvas.drawPath(
                (from.x * box) + (box / 2) + xOffs,
                (from.y * box) - (box / 2) + yOffs,
                (to.x * box) + (box / 2) + xOffs,
                (to.y * box) - (box / 2) + yOffs,
            )
        }
    }
}

class DrawableCanvas(private val width: Int, height: Int) : JPanel() {
    private val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    init {
        preferredSize = Dimension(width, height)
        isVisible = true
    }

    override fun paint(g: Graphics) {
        g.drawImage(img, 0, 0, width, height, null)
    }

    fun setPixel(x: Int, y: Int) {
        img.createGraphics().apply { drawLine(x, y, x, y) }
        repaint()
    }

    fun drawString(x: Int, y: Int, string: String) {
        img.createGraphics().apply {
            font = Font("Andale Mono", Font.PLAIN, 10)
            background = Color.GRAY
            drawString(string, x, y)
        }
        repaint()
    }

    fun drawPath(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int) {
        img.createGraphics().apply { drawLine(xFrom, yFrom, xTo, yTo) }

        repaint()
    }

    fun clear() {
        img.createGraphics().clearRect(0, 0, width, height)
        repaint()
    }
}

private fun createBasicFrame(width: Int, height: Int): JFrame {
    val frame = JFrame()

    frame.setSize(width, height)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
    return frame
}
