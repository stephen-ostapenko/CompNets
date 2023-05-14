import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlin.concurrent.thread

@Composable
@Preview
fun app() {
    var host by remember { mutableStateOf("127.0.0.1") }
    var port by remember { mutableStateOf("22222") }
    var useUDP by remember { mutableStateOf(false) }

    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

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
                    // host
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("host") },
                        enabled = !sending,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )

                    // port
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("port") },
                        enabled = !sending,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )

                    // protocol switch
                    Button(
                        onClick = {
                            useUDP = !useUDP
                        },
                        enabled = !sending,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .width(100.dp)
                    ) {
                        Text(if (useUDP) "UDP" else "TCP")
                    }
                }

                // send button
                Button(
                    onClick = {
                        sending = true
                        error = false

                        thread {
                            try {
                                sendPackets(host, port.toInt(), useUDP)
                            } catch (e: Exception) {
                                error = true

                                println(e)
                            } finally {
                                sending = false
                            }
                        }
                    },
                    colors = if (error) {
                        ButtonDefaults.buttonColors(MaterialTheme.colors.error)
                    } else {
                        ButtonDefaults.buttonColors(MaterialTheme.colors.primary)
                    },
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .width(150.dp)
                ) {
                    Text(
                        text = when {
                            sending -> "sending..."
                            error -> "error!"
                            else -> "send packets"
                        }
                    )
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SpeedTest client",
        state = rememberWindowState(width = 800.dp, height = 200.dp),
        resizable = false
    ) {
        app()
    }
}