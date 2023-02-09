package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.PLATFORM
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class AddBrowsePlaylistUsecase(
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val addPlaylistUsecase: AddPlaylistUsecase,
    private val connectivityWrapper: ConnectivityWrapper,
    private val log: LogWrapper,
) {

    init {
        log.tag(this)
    }

    suspend fun execute(category: CategoryDomain, parentId: Identifier<GUID>?): PlaylistDomain? {
        val platformId = category.platformId ?: return null
        platformId
            .let { playlistOrchestrator.loadByPlatformId(it, LOCAL.deepOptions(false)) }
            ?.also { log.d("found existing playlist = ${it.id}") }
            ?.also { return@execute (it) }

        if (!connectivityWrapper.isConnected()) {
            return null
        }

        return playlistOrchestrator.loadByPlatformId(platformId, PLATFORM.deepOptions(false))
            ?.also { log.d("${it.platformId}: ${it.title}") }
            ?.let {
                it.copy(
                    image = category.image ?: it.image,
                    thumb = category.image ?: it.thumb,
                    parentId = parentId,
                    // platformId = platformId,
                    config = it.config.copy(
                        description = category.description
                    )
                )
            }
            ?.let { addPlaylistUsecase.addPlaylist(it, null) }
    }
}