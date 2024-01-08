import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int

class CLArgs : CliktCommand() {
    override fun run() = Unit

    val name: String? by
        option("-n", "--name", help = "Your name")

    val host: String by
        option("-h", "--host", help = "Host address").required()

    val port: Int by
        option("-P", "--port", help = "Port").int().default(25)

    val username: String by
        option("-u", "--username", help = "Your username").required()

    val password: String by
        option("-p", "--password", help = "Your password").required()

    val recipientName: String? by
        option("-r", "--recipient", help = "Recipient name")

    val recipientAddress: String by
        option("-t", "--to", help = "Recipient address").required()

    val subject: String by
        option("-s", "--subject", help = "Mail subject").default("")

    val filePath: String by
        option("-f", "--file", help = "Letter file path").required()

    val useHTML: Boolean by
        option("-H", "--HTML", help = "Send letter with HTML body").flag()
}