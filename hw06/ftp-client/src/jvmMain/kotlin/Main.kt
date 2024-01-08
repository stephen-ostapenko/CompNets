package mkn.compnets

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

enum class State {
    Disconnected, Connected, Error
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun mainApp() {
    var host by remember { mutableStateOf("127.0.0.1") }
    var port by remember { mutableStateOf("21") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var state by remember { mutableStateOf(State.Disconnected) }

    lateinit var client: FtpClient

    val currentPathList = mutableListOf<String>()
    var currentPath by remember { mutableStateOf("/") }
    var currentDirs by remember { mutableStateOf(listOf<String>()) }
    var currentFiles by remember { mutableStateOf(listOf<String>()) }

    fun getCurrentPath(): String {
        if (currentPathList.isEmpty()) {
            return "/"
        }
        return currentPathList.joinToString("/", "/", "/")
    }

    fun updatePath() {
        currentPath = getCurrentPath()
        currentDirs = client.listDirectories(currentPath)
        currentFiles = client.listFiles(currentPath)
    }

    var selectedDir by remember { mutableStateOf<Int?>(null) }
    var selectedFile by remember { mutableStateOf<Int?>(null) }

    fun unselect() {
        selectedFile = null
        selectedDir = null
    }

    fun selectDir(index: Int) {
        selectedFile = null
        selectedDir = index
    }

    fun selectFile(index: Int) {
        selectedDir = null
        selectedFile = index
    }

    var createFileDialogIsOpen by remember { mutableStateOf(false) }
    var viewFileDialogIsOpen by remember { mutableStateOf(false) }
    var editFileDialogIsOpen by remember { mutableStateOf(false) }
    var deleteFileDialogIsOpen by remember { mutableStateOf(false) }

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
                    // host
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("host") },
                        enabled = (state != State.Connected),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )

                    // port
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("port") },
                        enabled = (state != State.Connected),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("username") },
                        enabled = (state != State.Connected),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )

                    // password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        enabled = (state != State.Connected),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // go back button
                    Button(
                        enabled = (state == State.Connected),
                        onClick = {
                            if (currentPathList.isEmpty()) {
                                return@Button
                            }

                            currentPathList.removeLast()
                            unselect()
                            updatePath()
                        }
                    ) {
                        Text("../")
                    }

                    // connect button
                    Button(
                        onClick = {
                            when (state) {
                                State.Connected -> {
                                    client.close()
                                    state = State.Disconnected

                                    unselect()
                                    currentDirs = listOf()
                                    currentFiles = listOf()
                                }
                                else -> {
                                    try {
                                        client = FtpClient(host, port.toInt())
                                        client.open(username, password)
                                        state = State.Connected

                                        currentPathList.clear()
                                        unselect()
                                        updatePath()
                                    } catch (e: Exception) {
                                        state = State.Error
                                    }
                                }
                            }
                        },
                        colors = if (state == State.Error) {
                            ButtonDefaults.buttonColors(Color.Red)
                        } else {
                            ButtonDefaults.buttonColors(MaterialTheme.colors.primary)
                        },
                        modifier = Modifier
                            .width(200.dp)
                    ) {
                        when (state) {
                            State.Disconnected -> Text("connect")
                            State.Connected -> Text("disconnect")
                            State.Error -> Text("connect")
                        }
                    }

                    // refresh button
                    Button(
                        enabled = (state == State.Connected),
                        onClick = {
                            unselect()
                            updatePath()
                        }
                    ) {
                        Text("refresh")
                    }
                }

                // path display
                Text(
                    text = if (state == State.Connected) currentPath else "",
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(4.dp))
                        .padding(10.dp)
                )

                // files list
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    val currentBoxHeight = maxHeight

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .requiredHeight(currentBoxHeight - 80.dp)
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(1.dp, Color.Gray),
                                    RoundedCornerShape(5.dp)
                                )
                                .padding(10.dp)
                                .padding(bottom = 30.dp)
                        ) {
                            if (state == State.Connected) {
                                // directories
                                itemsIndexed(currentDirs) { index, name ->
                                    Text(
                                        text = "$name/",
                                        modifier = Modifier
                                            .background(
                                                if (index == selectedDir) {
                                                    MaterialTheme.colors.secondary
                                                } else {
                                                    Color.Transparent
                                                },
                                                RoundedCornerShape(4.dp)
                                            )
                                            .fillMaxWidth()
                                            .selectable(
                                                selected = (index == selectedDir),
                                                enabled = true
                                            ) {
                                                if (index == selectedDir) {
                                                    currentPathList.add(name)
                                                    unselect()
                                                    updatePath()
                                                } else {
                                                    selectDir(index)
                                                }
                                            }
                                            .padding(2.dp)
                                    )
                                }

                                // files
                                itemsIndexed(currentFiles) { index, name ->
                                    Text(
                                        text = name,
                                        modifier = Modifier
                                            .background(
                                                if (index == selectedFile) {
                                                    MaterialTheme.colors.secondary
                                                } else {
                                                    Color.Transparent
                                                },
                                                RoundedCornerShape(4.dp)
                                            )
                                            .fillMaxWidth()
                                            .selectable(
                                                selected = (index == selectedFile),
                                                enabled = true
                                            ) {
                                                selectFile(index)
                                            }
                                            .padding(2.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // create file button
                            Button(
                                enabled = (state == State.Connected),
                                onClick = {
                                    createFileDialogIsOpen = true
                                },
                                modifier = Modifier.width(120.dp)
                            ) {
                                Text("create file")
                            }

                            // view file button
                            Button(
                                enabled = (state == State.Connected && selectedFile != null),
                                onClick = {
                                    viewFileDialogIsOpen = true
                                },
                                modifier = Modifier.width(120.dp)
                            ) {
                                Text("view file")
                            }

                            // edit file button
                            Button(
                                enabled = (state == State.Connected && selectedFile != null),
                                onClick = {
                                    editFileDialogIsOpen = true
                                },
                                modifier = Modifier.width(120.dp)
                            ) {
                                Text("edit file")
                            }

                            // delete file button
                            Button(
                                enabled = (state == State.Connected && selectedFile != null),
                                onClick = {
                                    deleteFileDialogIsOpen = true
                                },
                                modifier = Modifier.width(120.dp)
                            ) {
                                Text("delete file")
                            }
                        }

                        // status sign
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, end = 10.dp)
                        ) {
                            when (state) {
                                State.Disconnected -> Text("disconnected")
                                State.Connected -> Text("connected")
                                State.Error -> Text("error")
                            }
                        }
                    }
                }

                @Composable
                fun errorText() {
                    Text(
                        text = "error!",
                        textAlign = TextAlign.Center,
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                }

                // create file dialog
                if (createFileDialogIsOpen) {
                    AlertDialog(
                        onDismissRequest = {
                            createFileDialogIsOpen = false
                        },
                        buttons = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "enter file name",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(10.dp)
                                )

                                var fileName by remember { mutableStateOf("") }
                                TextField(
                                    value = fileName,
                                    onValueChange = { fileName = it },
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp)
                                )

                                var error by remember { mutableStateOf(false) }

                                Button(
                                    onClick = {
                                        try {
                                            client.createFile(getCurrentPath() + fileName)
                                        } catch (e: Exception) {
                                            error = true
                                            return@Button
                                        }

                                        createFileDialogIsOpen = false
                                        unselect()
                                        updatePath()
                                    }
                                ) {
                                    Text("create file")
                                }

                                if (error) {
                                    errorText()
                                }
                            }
                        },
                        modifier = Modifier
                            .requiredSize(600.dp, 250.dp)
                    )
                }

                // view file dialog
                if (viewFileDialogIsOpen) {
                    AlertDialog(
                        onDismissRequest = {
                            viewFileDialogIsOpen = false
                        },
                        buttons = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val fileName = try {
                                    currentFiles[selectedFile ?: error("No file to display")]
                                } catch (e: Exception) {
                                    errorText()
                                    return@AlertDialog
                                }

                                Text(
                                    text = "file '$fileName'",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(10.dp)
                                )

                                val stream = ByteArrayOutputStream()
                                try {
                                    client.readFile(getCurrentPath() + fileName, stream)
                                } catch (e: Exception) {
                                    errorText()
                                    return@AlertDialog
                                }

                                Text(
                                    text = stream.toByteArray().decodeToString(),
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp)
                                        .verticalScroll(rememberScrollState())
                                )
                            }
                        },
                        modifier = Modifier
                            .requiredSize(600.dp, 400.dp)
                    )
                }

                // edit file dialog
                if (editFileDialogIsOpen) {
                    AlertDialog(
                        onDismissRequest = {
                            editFileDialogIsOpen = false
                        },
                        buttons = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val fileName = try {
                                    currentFiles[selectedFile ?: error("No file to display")]
                                } catch (e: Exception) {
                                    errorText()
                                    return@AlertDialog
                                }
                                val fullFilePath = getCurrentPath() + fileName

                                Text(
                                    text = "file '$fileName'",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(10.dp)
                                )

                                val stream = ByteArrayOutputStream()
                                try{
                                    client.readFile(fullFilePath, stream)
                                } catch (e: Exception) {
                                    errorText()
                                    return@AlertDialog
                                }

                                var error by remember { mutableStateOf(false) }
                                var fileContent by remember {
                                    mutableStateOf(stream.toByteArray().decodeToString())
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    OutlinedTextField(
                                        value = fileContent,
                                        onValueChange = { fileContent = it },
                                        label = { Text("file") },
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState())
                                            .weight(1f)
                                    )

                                    Button(
                                        onClick = {
                                            try {
                                                ByteArrayInputStream(fileContent.toByteArray()).use {
                                                    client.writeFile(fullFilePath, it)
                                                }
                                            } catch (e: Exception) {
                                                error = true
                                                return@Button
                                            }

                                            editFileDialogIsOpen = false
                                        },
                                        modifier = Modifier
                                            .weight(0.1f)
                                            .padding(top = 5.dp, bottom = 10.dp)
                                    ) {
                                        Text("save file")
                                    }

                                    if (error) {
                                        errorText()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .requiredSize(600.dp, 700.dp)
                    )
                }

                // delete file dialog
                if (deleteFileDialogIsOpen) {
                    AlertDialog(
                        onDismissRequest = {
                            deleteFileDialogIsOpen = false
                        },
                        buttons = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "delete this file?",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(10.dp)
                                )

                                var error by remember { mutableStateOf(false) }

                                Button(
                                    onClick = {
                                        try {
                                            val fileName = currentFiles[selectedFile ?: error("No files selected")]
                                            client.deleteFile(getCurrentPath() + fileName)
                                        } catch (e: Exception) {
                                            error = true
                                            return@Button
                                        }

                                        deleteFileDialogIsOpen = false
                                        unselect()
                                        updatePath()
                                    }
                                ) {
                                    Text("confirm")
                                }

                                if (error) {
                                    Text(
                                        text = "error!",
                                        textAlign = TextAlign.Center,
                                        color = Color.Red,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .requiredSize(200.dp, 100.dp)
                    )
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "FTP client",
        state = rememberWindowState(width = 650.dp, height = 800.dp),
    ) {
        window.minimumSize = Dimension(650, 600)
        mainApp()
    }
}
