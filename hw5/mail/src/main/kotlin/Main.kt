import com.github.ajalt.clikt.core.NoSuchParameter
import com.github.ajalt.clikt.core.PrintHelpMessage
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import java.io.File

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

    val username = arguments.username
    val recipient = arguments.recipientAddress

    val emailHeader = EmailBuilder
        .startingBlank()
        .from(
            arguments.name ?: username,
            username
        )
        .to(
            arguments.recipientName ?: recipient,
            recipient
        )
        .withSubject(arguments.subject)

    val letterBody = File(arguments.filePath)

    val email = if (arguments.useHTML) {
        emailHeader.withHTMLText(letterBody)
    } else {
        emailHeader.withPlainText(letterBody)
    }.buildEmail()

    MailerBuilder
        .withSMTPServer(
            arguments.host, arguments.port,
            username, arguments.password
        )
        .withTransportStrategy(TransportStrategy.SMTP_TLS)
        .buildMailer()
        .sendMail(email)

    println("Mail to $recipient sent successfully")
}