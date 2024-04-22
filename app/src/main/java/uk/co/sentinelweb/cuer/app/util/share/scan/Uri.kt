package uk.co.sentinelweb.cuer.app.util.share.scan

import java.net.URI
import java.net.URISyntaxException

// test class for replacing andriod class seems to parse ok but need to move to a jvm sourceset
// also make for ios - prompt:
//    I have this class and i want an equivalent that will work for ios write it in kotlin with the ios foundation apis

class Uri private constructor(private val uri: URI) {

    val scheme: String? get() = uri.scheme
    val authority: String? get() = uri.authority
    val path: String? get() = uri.path
    val query: String? get() = uri.query
    val fragment: String? get() = uri.fragment
    val userInfo: String? get() = uri.userInfo
    val host: String? get() = uri.host
    val port: Int get() = uri.port
    val lastPathSegment: String? get() = uri.path?.substringAfterLast("/")

    fun getQueryParameter(key: String): String? {
        return query?.split("&")
            ?.map { it.split("=") }
            ?.firstOrNull { it[0] == key }
            ?.getOrNull(1)
    }

    fun getQueryParameters(key: String): List<String> {
        return query
            ?.also { print("query: $it") }
            ?.split("&")
            ?.map { it.split("=") }
            ?.filter { it[0] == key }
            ?.also { print(it) }
            ?.mapNotNull { it.getOrNull(1) }
            ?: emptyList()
    }

    override fun toString() = uri.toString()

    companion object {
        fun parse(uriString: String): Uri {
            return try {
                Uri(URI(uriString)).apply { println(this) }
            } catch (e: URISyntaxException) {
                throw IllegalArgumentException("Invalid URI: $uriString", e)
            }
        }
    }
}