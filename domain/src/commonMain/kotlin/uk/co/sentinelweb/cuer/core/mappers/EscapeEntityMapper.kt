package uk.co.sentinelweb.cuer.net.mappers

class EscapeEntityMapper {
    fun map(withEntities: String): String = withEntities
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
}