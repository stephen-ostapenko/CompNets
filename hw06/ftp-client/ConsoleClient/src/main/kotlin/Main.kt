package mkn.compnets

import com.github.ajalt.clikt.core.NoSuchParameter
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.UsageError
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream

fun listDirectory(client: FtpClient, path: String) {
    val directories = client.listDirectories(path)
    println("${directories.size} directories:")
    directories.forEach {
        println(it)
    }

    println()

    val files = client.listFiles(path)
    println("${files.size} files:")
    files.forEach {
        println(it)
    }
}

fun readFile(client: FtpClient, path: String) {
    client.readFile(path, System.out)
    println("\n>>> File '$path' read successfully")
}

fun downloadFile(client: FtpClient, pathSrc: String, pathDst: String) {
    client.readFile(pathSrc, BufferedOutputStream(FileOutputStream(pathDst)))
    println("File '$pathSrc' successfully downloaded to '$pathDst'")
}

fun uploadFile(client: FtpClient, pathSrc: String, pathDst: String) {
    client.writeFile(pathDst, BufferedInputStream(FileInputStream(pathSrc)))
    println("File '$pathSrc' successfully uploaded to '$pathDst'")
}

fun getServerAndLocalPaths(s: String): Pair<String, String> {
    val paths = s.split(":")
    if (paths.size != 2) {
        throw IllegalArgumentException("Wrong path-line $s (must be <path to file on server>:<path to file on local machine>)")
    }

    return paths[0] to paths[1]
}

fun main(args: Array<String>) {
    val arguments = CLArgs()
    try {
        arguments.parse(args)
    } catch (e: PrintHelpMessage) {
        println(arguments.getFormattedHelp())
        return
    } catch (e: NoSuchParameter) {
        println(e.message)
        return
    } catch (e: UsageError) {
        println(e.message)
        return
    } catch (e: Exception) {
        println("Error!\n$e")
        return
    }

    try {
        FtpClient(arguments.host, arguments.port, arguments.verbose).use {
            it.open(arguments.username, arguments.password)

            when (arguments.action) {
                Action.List -> listDirectory(it, arguments.path)
                Action.Read -> readFile(it, arguments.path)
                Action.Download -> {
                    val (serverPath, localPath) = getServerAndLocalPaths(arguments.path)
                    downloadFile(it, serverPath, localPath)
                }
                Action.Upload -> {
                    val (serverPath, localPath) = getServerAndLocalPaths(arguments.path)
                    uploadFile(it, localPath, serverPath)
                }
            }
        }
    } catch (e: Exception) {
        println(e)
    }
}