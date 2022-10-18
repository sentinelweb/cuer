package uk.co.sentinelweb.cuer.domain.ext

import summarise
import uk.co.sentinelweb.cuer.domain.MediaDomain

fun MediaDomain.stringMedia(): String = "id=$id title=$title platform=$platform platformId=$platformId"

fun MediaDomain.isLiveOrUpcoming() = isLiveBroadcastUpcoming || isLiveBroadcast

fun MediaDomain.startPosition(): Long {
    val position = positon ?: -1L
    val duration = duration ?: -1L
    return if (position > 0 && (duration > 0 && position < duration - 20000)) {
        position
    } else {
        0L
    }
}

fun MediaDomain.summarise(): String = """
    id: $id, title: $title, platform: $platform - $platformId, [channel: ${channelData.summarise()}]
""".trimIndent()