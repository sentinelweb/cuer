package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

interface AppPlaylistInteractor {
    suspend fun getPlaylist(): PlaylistDomain?
    fun makeHeader(): PlaylistDomain
    fun makeStats(): PlaylistStatDomain
}