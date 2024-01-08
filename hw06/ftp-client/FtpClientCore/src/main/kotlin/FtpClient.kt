package mkn.compnets

import org.apache.commons.net.PrintCommandListener
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.*
import java.nio.file.FileAlreadyExistsException
import java.util.*
import java.util.stream.Collectors

class FtpClient(
    private val host: String, private val port: Int,
    private val verbose: Boolean = false
): Closeable {
    private val client = FTPClient()

    fun open(username: String, password: String) {
        if (verbose) {
            client.addProtocolCommandListener(PrintCommandListener(PrintWriter(System.out)))
        }

        client.connect(host, port)
        if (!FTPReply.isPositiveCompletion(client.replyCode)) {
            client.disconnect()
            throw IOException("Can't connect to server (reply code: ${client.replyCode})")
        }

        client.login(username, password)
        if (!FTPReply.isPositiveCompletion(client.replyCode)) {
            client.disconnect()
            throw IOException("Can't login to server (reply code: ${client.replyCode})")
        }

        client.setFileType(FTP.BINARY_FILE_TYPE)
    }

    override fun close() {
        client.disconnect()
    }

    fun listFiles(path: String): List<String> {
        val files = client.listFiles(path) { it.isFile }

        if (!FTPReply.isPositiveCompletion(client.replyCode)) {
            throw IOException("Can't list files on server (reply code: ${client.replyCode})")
        }

        return Arrays.stream(files)
            .map { it.name }
            .collect(Collectors.toList())
            .sorted()
    }

    fun listDirectories(path: String): List<String> {
        val files = client.listDirectories(path)

        if (!FTPReply.isPositiveCompletion(client.replyCode)) {
            throw IOException("Can't list directories on server (reply code: ${client.replyCode})")
        }

        return Arrays.stream(files)
            .map { it.name }
            .collect(Collectors.toList())
            .sorted()
    }

    fun readFile(path: String, destination: OutputStream) {
        client.retrieveFile(path, destination)
        destination.flush()

        if (!FTPReply.isPositiveCompletion(client.replyCode)) {
            throw IOException("Can't read file from server (reply code: ${client.replyCode})")
        }
    }

    fun writeFile(path: String, from: InputStream) {
        client.storeFile(path, from)

        if (!FTPReply.isPositiveCompletion(client.replyCode)) {
            throw IOException("Can't write file to server (reply code: ${client.replyCode})")
        }
    }

    fun createFile(path: String) {
        val files = try {
            listFiles(path)
        } catch (e: IOException) {
            if (client.replyCode != 550) {
                throw e
            }
            listOf()
        }
        if (files.isNotEmpty()) {
            throw FileAlreadyExistsException("File $path already exists")
        }

        ByteArrayInputStream("".toByteArray()).use {
            client.storeFile(path, it)
        }

        if (!FTPReply.isPositiveCompletion(client.replyCode)) {
            println(client.replyCode)
            throw IOException("Can't create file on server (reply code: ${client.replyCode})")
        }
    }

    fun deleteFile(path: String) {
        client.deleteFile(path)

        if (!FTPReply.isPositiveCompletion(client.replyCode)) {
            throw IOException("Can't delete file from server (reply code: ${client.replyCode})")
        }
    }
}