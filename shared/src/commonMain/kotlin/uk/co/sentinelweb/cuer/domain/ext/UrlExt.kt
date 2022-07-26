package uk.co.sentinelweb.cuer.domain.ext

private const val PROTO_SEPARATOR = "://"

fun urlDomain(url: String): String {
    val startIndex = url.indexOf(PROTO_SEPARATOR) + PROTO_SEPARATOR.length
    val endIndex = url.indexOf("/", startIndex)
        .takeIf { it != -1 }
        ?: url.length
    return url.substring(startIndex, endIndex)
}