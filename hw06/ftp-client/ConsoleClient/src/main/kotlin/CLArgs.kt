package mkn.compnets

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int

enum class Action {
    List, Read, Download, Upload
}

class CLArgs : CliktCommand() {
    override fun run() = Unit

    val action: Action by
        mutuallyExclusiveOptions(
            option("list").flag().convert { Action.List },
            option("read").flag().convert { Action.Read },
            option("download").flag().convert { Action.Download },
            option("upload").flag().convert { Action.Upload }
        ).single().required()

    val host: String by
        option("-h", "--host", help = "Host address").required()

    val port: Int by
        option("-P", "--port", help = "Port").int().default(21)

    val username: String by
        option("-u", "--username", help = "Username").required()

    val password: String by
        option("-p", "--password", help = "Password").required()

    val path: String by
        option("-f", "--path", help = """
            <path to folder on server> for listing;
            <path to file on server> for reading;
            <path to file on server>:<path to file on local machine> for downloading and uploading; 
        """.trimIndent()).required()

    val verbose: Boolean by
        option("--verbose", help = "Verbose output").flag()
}