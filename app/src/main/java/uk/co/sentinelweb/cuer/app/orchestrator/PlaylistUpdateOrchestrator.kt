package uk.co.sentinelweb.cuer.app.orchestrator

import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM

class PlaylistUpdateOrchestrator constructor(
    private val timeProvider: TimeProvider,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val updateChecker: UpdateCheck
) {

    fun checkToUpdate(p: PlaylistDomain): Boolean = updateChecker.shouldUpdate(p)

    suspend fun update(p: PlaylistDomain) {

    }

    companion object {
        private val UPDATE_INTERVAL_DEFAULT = 1000 * 60 * 60
    }

    interface UpdateCheck {
        fun shouldUpdate(p: PlaylistDomain): Boolean
    }

    class PlatformUpdateCheck : UpdateCheck {
        override fun shouldUpdate(p: PlaylistDomain): Boolean =
            p.type == PLATFORM

    }
}