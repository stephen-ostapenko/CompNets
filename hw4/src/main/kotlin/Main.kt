import java.io.IOException
import java.net.ServerSocket
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.writeText

fun main(args: Array<String>) {
    val pathToLog = Path.of(args[1])
    if (!pathToLog.exists()) {
        pathToLog.writeText("")
    }
    if (!pathToLog.toFile().canWrite()) {
        throw IOException("Log file can't be modified")
    }
    val logger = Logger(pathToLog)

    val pathToBlackList = Path.of(args[2])
    if (!pathToBlackList.toFile().exists()) {
        pathToBlackList.toFile().writeText("[]\n")
    }
    val filter = Filter(pathToBlackList)

    val pathToCache = Path.of(args[3])
    if (pathToCache.exists() && !pathToCache.isDirectory()) {
        throw IOException("Cache can't be stored")
    }
    if (!pathToCache.toFile().exists()) {
        pathToCache.toFile().mkdir()
    }
    if (!pathToCache.toFile().canWrite()) {
        throw IOException("Cache can't be stored")
    }
    val cache = Cache(pathToCache)

    val server = ServerSocket(args[0].toInt())
    println("server started on port ${server.localPort}")

    while (true) {
        val socket = server.accept()
        serveRequest(socket, filter, logger, cache)
    }
}