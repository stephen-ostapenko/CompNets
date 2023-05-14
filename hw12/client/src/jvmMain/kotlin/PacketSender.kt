import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.time.Instant
import kotlin.random.Random

const val PACKETS_CNT = 100
const val DATA_SIZE = 65_000
const val DATETIME_SIZE = 30

fun genRandomData(rnd: Random): ByteArray {
    return rnd.nextBytes(DATA_SIZE)
}

fun getCurrentTimeAsByteArray(): ByteArray {
    val time = Instant.now().toString()
    if (time.length == DATETIME_SIZE) {
        return time.encodeToByteArray()
    }

    return time
        .dropLast(1)
        .padEnd(DATETIME_SIZE - 1, '0')
        .padEnd(DATETIME_SIZE, 'Z')
        .encodeToByteArray()
}

fun sendPackets(host: String, port: Int, useUDP: Boolean) {
    if (!useUDP) {
        sendTCPPackets(host, port)
    } else {
        sendUDPPackets(host, port)
    }
}

fun sendTCPPackets(host: String, port: Int) {
    val socket = Socket(host, port)
    val outputStream = socket.getOutputStream()
    val rnd = Random(248)

    socket.use {
        for (i in 1..PACKETS_CNT) {
            outputStream.write(
                genRandomData(rnd).plus(getCurrentTimeAsByteArray())
            )
            outputStream.flush()
        }
    }
}

fun sendUDPPackets(host: String, port: Int) {
    val socket = DatagramSocket()
    val rnd = Random(248)

    socket.use {
        for (i in 1..PACKETS_CNT) {
            val packet = DatagramPacket(
                genRandomData(rnd).plus(getCurrentTimeAsByteArray()),
                DATA_SIZE + DATETIME_SIZE,
                InetAddress.getByName(host),
                port
            )

            socket.send(packet)
        }
    }
}