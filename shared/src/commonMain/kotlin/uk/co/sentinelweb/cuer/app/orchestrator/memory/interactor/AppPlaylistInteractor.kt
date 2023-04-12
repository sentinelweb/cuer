package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

interface AppPlaylistInteractor {
    val hasCustomDeleteAction: Boolean
    val customResources: CustomisationResources?

    suspend fun getPlaylist(): PlaylistDomain?
    fun makeHeader(): PlaylistDomain
    fun makeStats(): PlaylistStatDomain

    suspend fun performCustomDeleteAction(item: PlaylistItemDomain)

    interface CustomisationResources {
        val customDelete: ActionResources?
    }

    interface Updateable {
        fun update()
    }

}