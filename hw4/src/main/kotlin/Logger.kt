import org.apache.commons.httpclient.HttpStatus
import java.nio.file.Path
import java.time.LocalDateTime

class Logger(pathToLog: Path) {
    private val logFile = pathToLog.toFile()

    init {
        logFile.appendText("\nnew logger created at ${LocalDateTime.now()}\n")
    }

    fun newLine() {
        println()
        logFile.appendText("\n")
    }

    fun writeLine(line: String) {
        println("${LocalDateTime.now()}: $line")
        logFile.appendText("${LocalDateTime.now()}: $line\n")
    }

    fun writeStatusCode(code: Int) {
        println("${LocalDateTime.now()}: $code: ${HttpStatus.getStatusText(code)}")
        logFile.appendText("${LocalDateTime.now()}: $code: ${HttpStatus.getStatusText(code)}\n")
    }
}