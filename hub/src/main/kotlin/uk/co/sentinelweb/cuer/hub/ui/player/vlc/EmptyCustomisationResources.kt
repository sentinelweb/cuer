package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources

class EmptyCustomisationResources : AppPlaylistInteractor.CustomisationResources {
    override val customDelete: ActionResources? = null
}