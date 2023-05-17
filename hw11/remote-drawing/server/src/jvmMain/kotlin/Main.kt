import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.InputStream
import java.net.ServerSocket
import kotlin.concurrent.thread

const val WIDTH = 800
const val HEIGHT = 600

@Composable
@Preview
fun app(args: Array<String>) {
    val points = remember { mutableStateListOf<List<Offset>>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    var pointsVersion by remember { mutableStateOf(0L) }

    val port = args[0].toInt()
    val server = ServerSocket(port)

    fun addPoint(point: Offset) {
        synchronized(currentPoints) {
            currentPoints.add(point)
        }
        pointsVersion++
    }

    fun flushPoints() {
        synchronized(currentPoints) {
            synchronized(points) {
                points.add(currentPoints.toList())
                currentPoints.clear()
            }
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val update = pointsVersion

                synchronized(points) {
                    points.forEach {
                        drawPoints(it, PointMode.Polygon, Color.Blue, strokeWidth = 2f)
                    }
                }

                synchronized(currentPoints) {
                    drawPoints(currentPoints, PointMode.Polygon, Color.Blue, strokeWidth = 2f)
                }
            }
        }
    }

    fun getOffsetFromStream(stream: InputStream): Offset {
        val data1 = stream.readNBytes(4).toList().map { it.toUByte() }.reversed()
        val data2 = stream.readNBytes(4).toList().map { it.toUByte() }.reversed()

        var num1 = 0
        var num2 = 0

        for (b in data1) {
            num1 = (num1 shl 8) or b.toInt()
        }
        for (b in data2) {
            num2 = (num2 shl 8) or b.toInt()
        }

        return Offset(num1.toFloat(), num2.toFloat())
    }

    thread {
        server.use {
            val socket = server.accept()
            val inputStream = socket.getInputStream()

            while (true) {
                when (inputStream.read()) {
                    -1   -> return@thread
                     1   -> addPoint(getOffsetFromStream(inputStream))
                     2   -> flushPoints()
                    else -> error("wrong control byte")
                }
            }
        }
    }
}

fun main(args: Array<String>) = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "drawing server",
        state = rememberWindowState(width = WIDTH.dp, height = HEIGHT.dp),
        resizable = false
    ) {
        app(args)
    }
}