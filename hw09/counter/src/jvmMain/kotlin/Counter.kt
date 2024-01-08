import java.net.*
import kotlin.concurrent.thread

const val MAX_MESSAGE_LENGTH = 64

fun getLocalAddress(interfaceName: String): InterfaceAddress {
    return NetworkInterface
        .networkInterfaces()
        .toList()
        .first { it.name == interfaceName }
        .interfaceAddresses
        .last()
}

class Counter(
    private val interfaceName: String,
    private val port: Int,
    private val broadcastPort: Int,
    private val timeout: Int,
    private val iterationsToLive: Int,
    private val addAddress: (InetSocketAddress) -> Unit,
    private val removeAddress: (InetSocketAddress) -> Unit
) {
    private val fullLocalAddress = getLocalAddress(interfaceName)
    private val ownAddress = InetSocketAddress(fullLocalAddress.address, port)

    private val broadcastAddress = InetSocketAddress(
        if (interfaceName.startsWith("lo")) {
            InetAddress.getByName("127.255.255.255")
        } else {
            fullLocalAddress.broadcast
        },
        broadcastPort
    )
    private val broadcastSender = DatagramSocket(ownAddress)
    private val broadcastListener = DatagramSocket(null)

    init {
        broadcastSender.broadcast = true

        broadcastListener.reuseAddress = true
        broadcastListener.bind(broadcastAddress)
    }

    private enum class MessageType {
        ENTER, ALIVE, EXIT
    }

    private val enterBroadcastMessage = "+$ownAddress".encodeToByteArray()
    private val aliveBroadcastMessage = "=$ownAddress".encodeToByteArray()
    private val exitBroadcastMessage = "-$ownAddress".encodeToByteArray()

    private val counter = HashMap<InetSocketAddress, Int>()

    private val workers = mutableListOf<Thread>()

    private fun broadcastAddress(type: MessageType) {
        synchronized(broadcastSender) {
            when (type) {
                MessageType.ENTER -> broadcastSender.send(DatagramPacket(enterBroadcastMessage, enterBroadcastMessage.size, broadcastAddress))
                MessageType.ALIVE -> broadcastSender.send(DatagramPacket(aliveBroadcastMessage, aliveBroadcastMessage.size, broadcastAddress))
                MessageType.EXIT  -> broadcastSender.send(DatagramPacket(exitBroadcastMessage, exitBroadcastMessage.size, broadcastAddress))
            }
        }
    }

    private fun decodeMessage(p: DatagramPacket): Pair<InetSocketAddress, MessageType> {
        val message = p.data.decodeToString(p.offset, p.offset + p.length)
        val (host, port) = message.drop(2).split(":")
        return InetSocketAddress(host, port.toInt()) to when (message[0]) {
            '+' -> MessageType.ENTER
            '=' -> MessageType.ALIVE
            '-' -> MessageType.EXIT
            else -> error("wrong message type char")
        }
    }

    fun run() {
        thread {
            val enterTimeout = 50
            broadcastAddress(MessageType.ENTER)

            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < enterTimeout) {
                val p = DatagramPacket(ByteArray(MAX_MESSAGE_LENGTH), MAX_MESSAGE_LENGTH)
                val timeLeft = enterTimeout - (System.currentTimeMillis() - startTime).toInt()
                if (timeLeft <= 0) {
                    break
                }
                broadcastListener.soTimeout = timeLeft

                try {
                    broadcastListener.receive(p)
                } catch (e: SocketTimeoutException) {
                    break
                }

                val (address, type) = decodeMessage(p)
                if (type == MessageType.EXIT) {
                    continue
                }

                if (counter.containsKey(address)) {
                    continue
                }
                counter[address] = -1
                addAddress(address)
            }

            counter[ownAddress] = 0
        }.join()

        workers.add(thread {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(timeout.toLong())
                } catch (e: InterruptedException) {
                    break
                }

                broadcastAddress(MessageType.ALIVE)
            }
        })

        workers.add(thread {
            fun doOneIteration(): Boolean {
                val aliveAddresses = HashSet<InetSocketAddress>()
                val startTime = System.currentTimeMillis()

                while (System.currentTimeMillis() - startTime < timeout) {
                    if (Thread.interrupted()) {
                        return true
                    }

                    val p = DatagramPacket(ByteArray(MAX_MESSAGE_LENGTH), MAX_MESSAGE_LENGTH)
                    val timeLeft = timeout - (System.currentTimeMillis() - startTime).toInt()
                    if (timeLeft <= 0) {
                        break
                    }
                    broadcastListener.soTimeout = timeLeft

                    try {
                        broadcastListener.receive(p)
                    } catch (e: SocketTimeoutException) {
                        break
                    }

                    val (address, type) = decodeMessage(p)
                    when (type) {
                        MessageType.ENTER -> {
                            broadcastAddress(MessageType.ALIVE)

                            if (!counter.containsKey(address)) {
                                counter[address] = -1
                                addAddress(address)
                            }
                        }
                        MessageType.ALIVE -> {
                            aliveAddresses.add(address)
                        }
                        MessageType.EXIT -> {
                            counter.remove(address)
                            removeAddress(address)
                        }
                    }
                }

                counter.forEach { (k, v) ->
                    val newValue = if (k in aliveAddresses || k == ownAddress) {
                        0
                    } else {
                        v + 1
                    }
                    counter[k] = newValue

                    if (newValue >= iterationsToLive) {
                        removeAddress(k)
                    }
                }

                counter.entries.removeAll {
                    it.value >= iterationsToLive
                }

                return Thread.interrupted()
            }

            try {
                while (!Thread.interrupted()) {
                    if (doOneIteration()) {
                        break
                    }
                }
            } finally {
                broadcastAddress(MessageType.EXIT)
            }
        })
    }

    fun stop() {
        workers.forEach {
            it.interrupt()
            it.join()
        }

        broadcastSender.close()
        broadcastListener.close()
    }
}