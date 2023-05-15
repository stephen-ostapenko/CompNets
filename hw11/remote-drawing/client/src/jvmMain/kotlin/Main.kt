import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.net.Socket

const val WIDTH = 800
const val HEIGHT = 600

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun app(args: Array<String>) {
    val points by remember { mutableStateOf(mutableListOf<List<Offset>>()) }
    val currentPoints by remember { mutableStateOf(mutableListOf<Offset>()) }
    var pointsVersion by remember { mutableStateOf(0L) }

    var brushDown by remember { mutableStateOf(false) }

    val socket = Socket(args[0], args[1].toInt())
    val outputStream = socket.getOutputStream()

    fun offsetToBytes(offset: Offset): ByteArray {
        var x = offset.x.toInt()
        var y = offset.y.toInt()

        val result = mutableListOf<Byte>()
        result.add(1)

        for (i in 1..4) {
            result.add((x and 255).toByte())
            x = x shr 8
        }

        for (i in 1..4) {
            result.add((y and 255).toByte())
            y = y shr 8
        }

        return result.toByteArray()
    }

    fun addPoint(point: Offset) {
        currentPoints.add(point)
        pointsVersion++

        outputStream.write(offsetToBytes(point))
        outputStream.flush()
    }

    fun flushPoints() {
        points.add(currentPoints.toList())
        currentPoints.clear()

        outputStream.write(2)
        outputStream.flush()
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onPointerEvent(PointerEventType.Press) {
                        addPoint(it.changes.first().position)
                        brushDown = true
                    }
                    .onPointerEvent(PointerEventType.Release) {
                        addPoint(it.changes.first().position)
                        brushDown = false
                        flushPoints()
                    }
                    .onPointerEvent(PointerEventType.Move) {
                        if (brushDown) {
                            addPoint(it.changes.first().position)
                        }
                    }
            ) {
                val update = pointsVersion

                points.forEach {
                    drawPoints(it, PointMode.Polygon, Color.Red, strokeWidth = 2f)
                }
                drawPoints(currentPoints, PointMode.Polygon, Color.Red, strokeWidth = 2f)
            }
        }
    }
}

fun main(args: Array<String>) = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "drawing client",
        state = rememberWindowState(width = WIDTH.dp, height = HEIGHT.dp),
        resizable = false
    ) {
        app(args)
    }
}