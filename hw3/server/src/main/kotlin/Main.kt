import com.github.ajalt.clikt.core.NoSuchParameter
import com.github.ajalt.clikt.core.PrintHelpMessage
import org.apache.commons.httpclient.HttpStatus
import rawhttp.core.RawHttp
import rawhttp.core.body.BytesBody
import java.io.File
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

fun getHTTPHeader(statusCode: Int): String {
    return """
        HTTP/1.1 $statusCode ${HttpStatus.getStatusText(statusCode)}
        Content-Type: plain/text
    """.trimIndent()
}

fun serveRequest(socket: Socket) = socket.use {
    val rawHttp = RawHttp()
    val inputStream = socket.getInputStream()
    val outputStream = socket.getOutputStream()
    val request = rawHttp.parseRequest(inputStream)

    println()
    println("serving request from ${socket.inetAddress.hostAddress} on thread '${Thread.currentThread().name}'")
    println("file: '${request.uri.path}'")

    val file = File(request.uri.path)

    if (!file.exists() || !file.isFile) {
        println("404 Not Found")

        rawHttp
            .parseResponse(getHTTPHeader(404))
            .writeTo(outputStream)

        return@use
    }

    val fileBytes = file.readBytes()
    rawHttp
        .parseResponse(getHTTPHeader(200))
        .withBody(BytesBody(fileBytes))
        .writeTo(outputStream)

    println("200 OK")
}

fun runServerOnSingleThread(server: ServerSocket) {
    while (true) {
        val socket = server.accept()
        serveRequest(socket)
    }
}

fun runServerOnThreadPool(server: ServerSocket, concurrencyLevel: Int) {
    val threadPool = Executors.newFixedThreadPool(concurrencyLevel)

    while (true) {
        val socket = server.accept()
        threadPool.submit {
            serveRequest(socket)
        }
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

    val server = ServerSocket(arguments.port)
    println("starting server with concurrency level ${arguments.concurrencyLevel} on port: ${server.localPort}")

    if (arguments.concurrencyLevel == 0) {
        runServerOnSingleThread(server)
    } else {
        runServerOnThreadPool(server, arguments.concurrencyLevel)
    }
}