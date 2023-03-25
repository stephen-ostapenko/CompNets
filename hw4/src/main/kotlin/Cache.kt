import rawhttp.core.RawHttp
import rawhttp.core.RawHttpResponse
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

class Cache(private val pathToCache: Path) {
    private val cachedResponses = Files.list(pathToCache)
        .map { it.name }
        .toList()
        .toMutableSet()

    fun getCachedResponse(url: String): RawHttpResponse<*>? {
        val cachedResponseFile = url.replace("/", "*")
        if (cachedResponseFile !in cachedResponses) {
            return null
        }

        return RawHttp().parseResponse(File(pathToCache.toFile(), cachedResponseFile))
    }

    fun saveResponse(url: String, response: RawHttpResponse<*>) {
        val responseFile = url.replace("/", "*")
        response.writeTo(File(pathToCache.toFile(), responseFile).outputStream())
        cachedResponses.add(responseFile)
    }
}