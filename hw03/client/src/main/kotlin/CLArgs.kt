import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int

class CLArgs : CliktCommand() {
    override fun run() = Unit

    val hostAddress: String by
        option("-a", "--host-address", help = "Server address")
            .required()

    val port: Int by
        option("-p", "--port", help = "Server port")
            .int()
            .required()

    val filePath: String by
        option("-f", "--file", help = "File path")
            .default("/")

    val localFilePath: String? by
        option("-s", "--save-to", help = "Path to save requested file")
}