package uk.co.sentinelweb.cuer.app.orchestrator.util

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.*

class PlaylistMergeOrchestrator constructor(
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    fun checkMerge(toDelete: PlaylistDomain, toReceive: PlaylistDomain): Boolean =
        (toDelete.config.updateUrl == null || toReceive.config.updateUrl == null)
                && toDelete.id != null && toReceive.id != null
                && toDelete.type != APP && toReceive.type != APP
                && toDelete.id != toReceive.id
                && ((toDelete.type == PLATFORM && toReceive.type == USER) || (toDelete.type == USER && toReceive.type == PLATFORM))

    suspend fun merge(toDelete: PlaylistDomain, toReceive: PlaylistDomain): PlaylistDomain {
        if (!checkMerge(toDelete, toReceive)) throw IllegalArgumentException("Cannot merge these playlists")
        val toDeleteFull = playlistOrchestrator.load(toDelete.id!!, LOCAL.deepOptions())!!
        val toReceiveFull = playlistOrchestrator.load(toReceive.id!!, LOCAL.deepOptions())!!
        val merged = toReceiveFull.copy(
            type = if (toDeleteFull.type == PLATFORM) PLATFORM else toReceiveFull.type,
            channelData = if (toDeleteFull.type == PLATFORM && toReceiveFull.type == USER) toDeleteFull.channelData else toReceiveFull.channelData,
            platformId = if (toDeleteFull.type == PLATFORM && toReceiveFull.type == USER) toDeleteFull.platformId else toReceiveFull.platformId,

            items = toReceiveFull.items.toMutableList()
                .apply { addAll(toDeleteFull.items.map { it.copy(playlistId = toReceiveFull.id) }) },// copy also done in db
            config = toReceiveFull.config.copy(
                updateInterval = toReceiveFull.config.updateInterval ?: toDeleteFull.config.updateInterval,
                updateUrl = toReceiveFull.config.updateUrl ?: toDeleteFull.config.updateUrl,
                platformUrl = toReceiveFull.config.platformUrl ?: toDeleteFull.config.platformUrl,
                deletableItems = toReceiveFull.config.deletableItems && toDeleteFull.config.deletableItems,
                editableItems = toReceiveFull.config.editableItems && toDeleteFull.config.editableItems,
                deletable = toReceiveFull.config.deletable && toDeleteFull.config.deletable,
                editable = toReceiveFull.config.editable && toDeleteFull.config.editable,
                playable = toReceiveFull.config.playable && toDeleteFull.config.playable,
                description = (toReceiveFull.config.description ?: "") +
                        (toDeleteFull.config.description?.let { "\n\nMerged:\n\n$it" } ?: "")
            )
        )
        //log.d("MERGED:" + merged.serialise())
        return playlistOrchestrator.save(merged, LOCAL.deepOptions())
            //.apply { log.d("SAVED:" + this.serialise()) }
            .also {
                if (toDeleteFull.config.deletable) {
                    playlistOrchestrator.delete(toDeleteFull, LOCAL.deepOptions())
                }
            }
    }

}