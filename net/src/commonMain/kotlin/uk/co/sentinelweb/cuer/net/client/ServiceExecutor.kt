package uk.co.sentinelweb.cuer.net.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.system.ResponseDomain
import uk.co.sentinelweb.cuer.remote.server.message.ResponseMessage

internal class ServiceExecutor(
    private val client: HttpClient,
    private val type: ServiceType,
    private val log: LogWrapper,
) {

    init {
        log.tag(this)
        log.tag += "-$type"
    }

    suspend inline fun <reified T : Any> get(
        path: String,
        urlParams: Map<String, Any?> = emptyMap(),
        headers: Map<String, Any?> = emptyMap(),
    ): T = try {
        val urlString = "${type.baseUrl}/$path"
        log.d("Execute: $urlString")
        val response: HttpResponse = client.get(urlString) {
            urlParams.forEach { parameter(it.key, it.value) }
            headers.forEach { header(it.key, it.value) }
        }
        if (response.status.value >= 400) {
            throw RequestFailureException(response.status.value, response.status.description)
        }
        response.body<T>()
    } catch (e: Exception) {
        log.e("get", e)
        throw e
    }

    suspend fun getResponseDomain(
        path: String,
        urlParams: Map<String, Any?> = emptyMap(),
        headers: Map<String, Any?> = emptyMap(),
    ): ResponseDomain = get(path, urlParams, headers)

    suspend fun getResponseMessage(
        path: String,
        urlParams: Map<String, Any?> = emptyMap(),
        headers: Map<String, Any?> = emptyMap(),
    ): ResponseMessage = get(path, urlParams, headers)

    suspend inline fun <reified T : Any> post(
        path: String,
        body: Any? = null,
        urlParams: Map<String, Any?> = emptyMap(),
        headers: Map<String, Any?> = emptyMap(),
    ): T = try {
        val response: HttpResponse = client.post("${type.baseUrl}/$path") {
            body?.let {
                contentType(ContentType.Application.Json.withParameter("charset", "utf-8"))
                setBody(body) // Let the Ktor client handle @Serializable classes.
            }
            urlParams.forEach { parameter(it.key, it.value) }
            headers.forEach { header(it.key, it.value) }
        }
        if (response.status.value >= 400) {
            throw RequestFailureException(response.status.value, response.status.description)
        }
        response.body<T>()
    } catch (e: Exception) {
        log.e("post", e)
        throw e
    }

    suspend inline fun <reified T : Any> post(
        path: String,
        urlParams: Map<String, Any?> = emptyMap(),
        headers: Map<String, Any?> = emptyMap(),
        postData: Domain
    ): T = try {
        val response: HttpResponse = client.post("${type.baseUrl}/$path") {
            contentType(ContentType.Application.Json)
            setBody(postData)
            urlParams.forEach { parameter(it.key, it.value) }
            headers.forEach { header(it.key, it.value) }
        }
        if (response.status.value >= 400) {
            throw RequestFailureException(response.status.value, response.status.description)
        }
        response.body<T>()
    } catch (e: Exception) {
        log.e("put", e)
        throw e
    }

    suspend fun postResponse(
        path: String,
        urlParams: Map<String, Any?> = emptyMap(),
        headers: Map<String, Any?> = emptyMap(),
        postData: Domain
    ): ResponseDomain = post(path, urlParams, headers, postData)

}
