import org.apache.commons.httpclient.HttpStatus
import rawhttp.core.*
import rawhttp.core.body.StringBody
import java.io.IOException
import java.net.Socket
import java.net.URI
import java.net.URISyntaxException

const val defaultHTTPPort = 80
val allowedMethods = listOf("GET", "POST", "HEAD")

fun getHTTPResponseLine(statusCode: Int): String {
    return """
        HTTP/1.1 $statusCode ${HttpStatus.getStatusText(statusCode)}
        Content-Type: plain/text
    """.trimIndent()
}

fun sendRequestFromProxy(request: RawHttpRequest, logger: Logger): RawHttpResponse<*> {
    val host = request.uri.host
    val port = if (request.uri.port != -1) {
        request.uri.port
    } else {
        defaultHTTPPort
    }

    val socket = try {
        Socket(host, port)
    } catch (e: IOException) {
        logger.writeStatusCode(404)

        return RawHttp()
            .parseResponse(getHTTPResponseLine(404)) // Not Found
    }

    val inputStream = socket.getInputStream()
    val outputStream = socket.getOutputStream()

    val response = socket.use {
        request.writeTo(outputStream)
        RawHttp().parseResponse(inputStream).eagerly()
    }

    logger.writeStatusCode(response.statusCode)
    return response
}

fun sendConditionalRequestFromProxy(url: String, request: RawHttpRequest, logger: Logger, cache: Cache): RawHttpResponse<*> {
    if (request.method != "GET") {
        return sendRequestFromProxy(request, logger)
    }

    val cachedResponseString = cache.getCachedResponse(url) ?: return sendRequestFromProxy(request, logger)
    val cachedResponse = RawHttp().parseResponse(cachedResponseString).eagerly()

    val lastModified = cachedResponse.headers["Last-Modified"].firstOrNull()
    val etag = cachedResponse.headers["ETag"].firstOrNull()

    val headerBuilder = RawHttpHeaders.newBuilder()
    if (lastModified != null) {
        headerBuilder.with("If-Modified-Since", lastModified)
    }
    if (etag != null) {
        headerBuilder.with("If-None-Match", etag)
    }

    val newRequest = request.withHeaders(request.headers.and(headerBuilder.build()))
    val response = sendRequestFromProxy(newRequest, logger)

    if (response.statusCode != 304) {
        return response
    }

    logger.writeLine("using cached response")
    return cachedResponse
}

fun serveRequest(socket: Socket, filter: Filter, logger: Logger, cache: Cache) = socket.use {
    val inputStream = it.getInputStream()
    val outputStream = it.getOutputStream()

    logger.newLine()
    logger.writeLine("serving request from ${socket.inetAddress}")

    val request = try {
        RawHttp().parseRequest(inputStream)
    } catch (e: Exception) {
        RawHttp()
            .parseResponse(getHTTPResponseLine(400)) // Bad Request
            .withBody(StringBody("Bad request:\n$e\n"))
            .writeTo(outputStream)

        logger.writeStatusCode(400)

        return@use
    }

    if (request.method !in allowedMethods) {
        RawHttp()
            .parseResponse(getHTTPResponseLine(405)) // Method Not Allowed
            .withBody(StringBody("Method ${request.method} is not allowed\n"))
            .writeTo(outputStream)

        logger.writeLine("${request.method} http://${request.uri.path}")
        logger.writeStatusCode(405)

        return@use
    }

    val url = request.uri.path.drop(1)
    if (!filter.checkAddress(url)) {
        RawHttp()
            .parseResponse(getHTTPResponseLine(403)) // Forbidden
            .withBody(StringBody("This page ($url) is blocked!\n"))
            .writeTo(outputStream)

        logger.writeLine("${request.method} $url")
        logger.writeStatusCode(403)

        return@use
    }

    val fullUrl = try {
        URI("http://$url")
    } catch (e: URISyntaxException) {
        RawHttp()
            .parseResponse(getHTTPResponseLine(400)) // Bad Request
            .withBody(StringBody("Bad request URL:\n$e\n"))
            .writeTo(outputStream)

        logger.writeLine("${request.method} $url")
        logger.writeStatusCode(400)

        return@use
    }

    logger.writeLine("${request.method} $fullUrl")

    val newRequest = request.withRequestLine(
        RequestLine(request.method, fullUrl, request.startLine.httpVersion)
    )

    val response = sendConditionalRequestFromProxy(url, newRequest, logger, cache)
    response.writeTo(outputStream)

    if (newRequest.method == "GET" && response.statusCode == 200) {
        cache.saveResponse(url, response)
    }
}