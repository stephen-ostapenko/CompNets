import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.net.InetSocketAddress

enum class AppState {
    IDLE, STARTING, WORKING, STOPPING, ERROR
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun app() {
    var interfaceName by remember { mutableStateOf("lo") }
    var port by remember { mutableStateOf("24240") }
    var broadcastPort by remember { mutableStateOf("22222") }

    var timeout by remember { mutableStateOf("1000") }
    var iterationsToLive by remember { mutableStateOf("3") }

    var state by remember { mutableStateOf(AppState.IDLE) }
    var errorMessage: String? by remember { mutableStateOf(null) }

    lateinit var counter: Counter
    val addressList = remember { mutableStateListOf<String>() }

    fun addAddress(address: InetSocketAddress) {
        addressList.add(address.toString())
    }

    fun removeAddress(address: InetSocketAddress) {
        addressList.remove(address.toString())
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // interface name
                    OutlinedTextField(
                        value = interfaceName,
                        onValueChange = { interfaceName = it },
                        label = { Text("interface name") },
                        enabled = (state == AppState.IDLE || state == AppState.ERROR),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )

                    // port
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("port") },
                        enabled = (state == AppState.IDLE || state == AppState.ERROR),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )

                    // broadcast port
                    OutlinedTextField(
                        value = broadcastPort,
                        onValueChange = { broadcastPort = it },
                        label = { Text("broadcast port") },
                        enabled = (state == AppState.IDLE || state == AppState.ERROR),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // timeout
                    OutlinedTextField(
                        value = timeout,
                        onValueChange = { timeout = it },
                        label = { Text("timeout") },
                        enabled = (state == AppState.IDLE || state == AppState.ERROR),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )

                    // iterations to live
                    OutlinedTextField(
                        value = iterationsToLive,
                        onValueChange = { iterationsToLive = it },
                        label = { Text("iterations to live") },
                        enabled = (state == AppState.IDLE || state == AppState.ERROR),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // connect button
                Button(
                    onClick = {
                        if (state == AppState.IDLE || state == AppState.ERROR) {
                            try {
                                state = AppState.STARTING

                                counter = Counter(
                                    interfaceName,
                                    port.toInt(),
                                    broadcastPort.toInt(),
                                    timeout.toInt(),
                                    iterationsToLive.toInt(),
                                    { address -> addAddress(address) },
                                    { address -> removeAddress(address) }
                                )

                                addressList.clear()
                                counter.run()

                                state = AppState.WORKING
                            } catch (e: Exception) {
                                state = AppState.ERROR

                                println(e)
                                errorMessage = e.toString()
                            }
                        } else {
                            state = AppState.STOPPING
                            counter.stop()
                            state = AppState.IDLE
                        }
                    },
                    colors = if (state == AppState.ERROR) {
                        ButtonDefaults.buttonColors(MaterialTheme.colors.error)
                    } else {
                        ButtonDefaults.buttonColors(MaterialTheme.colors.primary)
                    },
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .width(200.dp)
                        .height(50.dp)
                ) {
                    Text(
                        text = when (state) {
                            AppState.IDLE     -> "connect"
                            AppState.STARTING -> "starting..."
                            AppState.WORKING  -> "connected"
                            AppState.STOPPING -> "stopping..."
                            AppState.ERROR    -> "error!"
                        }
                    )
                }

                // error dialog
                if (errorMessage != null) {
                    AlertDialog(
                        text = {
                            Text("error: $errorMessage", color = MaterialTheme.colors.error)
                        },
                        onDismissRequest = { errorMessage = null },
                        buttons = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // address list
                LazyColumn(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize()
                        .border(
                            BorderStroke(1.dp, Color.Gray), RoundedCornerShape(5.dp)
                        )
                        .padding(10.dp)
                ) {
                    items(addressList) {
                        Text(it)
                    }
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "program copy counter",
        state = rememberWindowState(width = 960.dp, height = 400.dp),
        resizable = false
    ) {
        app()
    }
}
