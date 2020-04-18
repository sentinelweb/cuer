package uk.co.sentinelweb.cuer.domain

import java.net.URL
import java.time.Instant

data class MediaDomain(
    val url: URL,
    val type: MediaType,
    val title: String,
    val lengthMs: Long,
    val positonMs: Long,
    val dateLastPlayed: Instant
) {
    enum class MediaType {
        VIDEO, AUDIO, WEB
    }
}