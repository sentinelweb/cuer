package uk.co.sentinelweb.cuer.domain.ext

import rewriteIdsToSource
import summarise
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.REMOTE
import uk.co.sentinelweb.cuer.domain.*

fun MediaDomain.stringMedia(): String = "id=$id title=$title platform=$platform platformId=$platformId"

fun MediaDomain?.isLiveOrUpcoming(): Boolean = (this?.let { isLiveBroadcastUpcoming || isLiveBroadcast } ?: false)

fun MediaDomain.startPosition(): Long {
    val position = positon ?: -1L
    val duration = duration ?: -1L
    return if (!isLiveOrUpcoming() && position > 0 && (duration > 0 && position < duration - 20000)) {
        position
    } else {
        0L
    }
}

fun MediaDomain.summarise(): String = """
    MEDIA: id: $id,platform: $platform - $platformId,  title: $title, [channel: ${channelData.summarise()}]
""".trimIndent()

fun MediaDomain.rewriteIdsToSource(source: Source, locator: Locator?) = this.copy(
    id = this.id?.copy(source = source, locator = locator),
    channelData = this.channelData.rewriteIdsToSource(source, locator)
)
