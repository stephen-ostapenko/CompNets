import com.github.ajalt.clikt.core.NoSuchParameter
import com.github.ajalt.clikt.core.PrintHelpMessage
import rawhttp.core.RawHttp
import rawhttp.core.RawHttpResponse
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.Socket

fun dumpFile(response: RawHttpResponse<*>, localFilePath: String) {
    if (response.statusCode != 200) {
        println("can't save file because server returned ${response.statusCode}")
        return
    }

    val body = try {
        response.body.get()
    } catch (e: NoSuchElementException) {
        println("can't save file because response has no body")
        return
    }

    try {
        val localFile = File(localFilePath)
        localFile.writeBytes(body.asRawBytes())
        println("file saved as '$localFilePath'")
    } catch (e: IOException) {
        println("failed to save file")
        println(e.message)
    }
}

fun main(args: Array<String>) {
    val arguments = CLArgs()
    try {
        arguments.parse(args)
    } catch (e: PrintHelpMessage) {
        println(arguments.getFormattedHelp())
        return
    } catch (e: NoSuchParameter) {
        println("No such parameter")
        println("${e.message}\n")
        return
    } catch (e: Exception) {
        println("Error!")
        println("${e.message}\n")
        return
    }

    println("sending request for file '${arguments.filePath}' to ${arguments.hostAddress}:${arguments.port}")

    val rawHttp = RawHttp()

    val socket = try {
        Socket(arguments.hostAddress, arguments.port)
    } catch (e: ConnectException) {
        println("Connecting to server failed!")
        println(e.message)
        return
    }

    val filePath = if (arguments.filePath[0] == '/') {
        arguments.filePath.drop(1)
    } else {
        arguments.filePath
    }

    socket.use {
        rawHttp.parseRequest("""
            GET /$filePath HTTP/1.1
            Host: ${arguments.hostAddress}:${arguments.port}
        """.trimIndent()
        ).writeTo(socket.getOutputStream())

        val response = rawHttp.parseResponse(socket.getInputStream()).eagerly()

        println("\nresponse:")
        println(response.eagerly())

        if (arguments.localFilePath != null) {
            dumpFile(response, arguments.localFilePath!!)
        }
    }
}