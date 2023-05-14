import java.net.*
import java.time.Instant

const val PACKETS_CNT = 100
const val DATA_SIZE = 65_000
const val DATETIME_SIZE = 30

fun secondsSinceEpoch(instant: Instant): Double {
    return instant.epochSecond.toDouble() + instant.nano / 1e9
}

fun receivePackets(port: Int, useUDP: Boolean, speeds: MutableList<Double>) {
    if (!useUDP) {
        receiveTCPPackets(port, speeds)
    } else {
        receiveUDPPackets(port, speeds)
    }
}

fun receiveTCPPackets(port: Int, speeds: MutableList<Double>) {
    val server = ServerSocket(port)
    server.soTimeout = 100

    server.use {
        var socket: Socket
        while (true) {
            if (Thread.interrupted()) {
                return
            }

            try {
                socket = server.accept()
            } catch (e: SocketTimeoutException) {
                continue
            }

            break
        }
        val inputStream = socket.getInputStream()

        for (i in 1..PACKETS_CNT) {
            if (Thread.interrupted()) {
                return
            }

            val data = inputStream.readNBytes(DATA_SIZE + DATETIME_SIZE)
            val endTimestamp = secondsSinceEpoch(Instant.now())

            if (data.isEmpty()) {
                break
            }

            val beginTimestamp = secondsSinceEpoch(
                Instant.parse(
                    data.decodeToString(data.size - DATETIME_SIZE, data.size, true)
                )
            )

            speeds.add(data.size / (endTimestamp - beginTimestamp))
        }
    }
}

fun receiveUDPPackets(port: Int, speeds: MutableList<Double>) {
    val socket = DatagramSocket(port)
    socket.reuseAddress = true
    socket.soTimeout = 100

    socket.use {
        var received = 0
        while (received < PACKETS_CNT) {
            if (Thread.interrupted()) {
                return
            }

            val data = ByteArray(DATA_SIZE + DATETIME_SIZE)
            val packet = DatagramPacket(data, data.size)

            try {
                socket.receive(packet)
            } catch (e: SocketTimeoutException) {
                continue
            }

            val endTimestamp = secondsSinceEpoch(Instant.now())

            if (data.isEmpty()) {
                continue
            }
            received++

            val beginTimestamp = secondsSinceEpoch(
                Instant.parse(
                    data.decodeToString(data.size - DATETIME_SIZE, data.size, true)
                )
            )

            speeds.add(data.size / (endTimestamp - beginTimestamp))
        }
    }
}