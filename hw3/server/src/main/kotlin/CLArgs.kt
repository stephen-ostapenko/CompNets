import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo

class CLArgs : CliktCommand() {
    override fun run() = Unit

    val concurrencyLevel: Int by
        option("-c", "--concurrency-level", help = "Server concurrency level")
            .int()
            .restrictTo(min = 0)
            .default(1)

    val port: Int by
        option("-p", "--port", help = "Port for server to listen")
            .int()
            .default(0)
}