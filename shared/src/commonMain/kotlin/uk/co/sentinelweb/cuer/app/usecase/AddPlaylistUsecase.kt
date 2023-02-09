package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class AddPlaylistUsecase(
    private val playlistMediaLookupUsecase: PlaylistMediaLookupUsecase,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val recentLocalPlaylists: RecentLocalPlaylists,
    private val log: LogWrapper,
) {

    init {
        log.tag(this)
    }

    suspend fun addPlaylist(
        playlistDomain: PlaylistDomain,
        addPlaylistParent: Identifier<GUID>?
    ): PlaylistDomain = playlistDomain
        .let { playlistMediaLookupUsecase.lookupMediaAndReplace(it) }
//            ?.also { log.i("lookupMediaAndReplace: $it") }
        .let {
            it.copy(
                items = it.items.map { it.copy(id = null) },
                config = it.config.copy(
                    playable = true,
                    editable = true,
                    deletable = true,
                    deletableItems = true,
                    editableItems = true
                )
            )
        }
        .let { playlist ->
            addPlaylistParent
                ?.let { parentId ->
                    playlistOrchestrator.loadById(parentId.id, parentId.source.flatOptions())
                        ?.let { playlist.copy(parentId = parentId) }
                }
                ?: playlist
        }
        .let { playlistOrchestrator.save(it, OrchestratorContract.Source.LOCAL.deepOptions(emit = true)) }
        .also { recentLocalPlaylists.addRecentId(it.id!!.id) }
}