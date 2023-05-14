import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlin.concurrent.thread

@Composable
@Preview
fun app() {
    var port by remember { mutableStateOf("22222") }

    var useUDP by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }

    val speeds by remember { mutableStateOf(mutableListOf<Double>()) }
    var receivingThread: Thread? by remember { mutableStateOf(null) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                ) {
                    // port
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("port") },
                        enabled = !listening,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )

                    // protocol switch
                    Button(
                        onClick = {
                            useUDP = !useUDP
                        },
                        enabled = !listening,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .width(100.dp)
                    ) {
                        Text(if (useUDP) "UDP" else "TCP")
                    }
                }

                // receive button
                Button(
                    onClick = {
                        if (!listening) {
                            listening = true
                            receivingThread = thread {
                                speeds.clear()
                                receivePackets(22222, useUDP, speeds)
                                listening = false
                            }
                        } else {
                            receivingThread?.interrupt()

                            thread(
                                isDaemon = true
                            ) {
                                receivingThread?.join()
                                receivingThread = null

                                listening = false
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .width(200.dp)
                ) {
                    Text(if (!listening) "receive packets" else "stop")
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // packets cnt
                    Text(
                        text = "packets: ${speeds.size} / $PACKETS_CNT",
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .border(BorderStroke(2.dp, Color.LightGray), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    )

                    // current speed
                    Text(
                        text = "speed: ${"%.3f".format(speeds.average() / 1e6)} MB/s",
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .border(BorderStroke(2.dp, Color.LightGray), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    )
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SpeedTest server",
        state = rememberWindowState(width = 600.dp, height = 250.dp),
        resizable = false
    ) {
        app()
    }
}