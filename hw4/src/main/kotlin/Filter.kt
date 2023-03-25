import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.net.URI
import java.nio.file.Path

@Serializable
data class BannedAddress(@Required val address: String, val banHost: Boolean = false)

class Filter(pathToBlackList: Path) {
    @OptIn(ExperimentalSerializationApi::class)
    private val bannedAddressList: List<BannedAddress> =
        Json.decodeFromStream(BufferedInputStream(FileInputStream(pathToBlackList.toFile())))

    init {
        println("loaded black list:")
        bannedAddressList.forEach {
            println(it)
        }
        println()
    }

    private fun checkURL(url: String, bannedAddress: BannedAddress): Boolean {
        return url != bannedAddress.address
    }

    private fun checkHost(url: String, bannedAddress: BannedAddress): Boolean {
        return URI("http://$url").host != bannedAddress.address
    }

    fun checkAddress(url: String): Boolean {
        return bannedAddressList.all {
            if (it.banHost) {
                checkHost(url, it)
            } else {
                checkURL(url, it)
            }
        }
    }
}