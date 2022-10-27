package uk.co.sentinelweb.cuer.app.ui.resources

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class StarredPlaylistCustomisationResources(
    private val res: ResourceWrapper
) : AppPlaylistInteractor.CustomisationResources {
    override val customDelete = object : ActionResources {
        override val label = res.getString(R.string.menu_unstar)
        override val icon = R.drawable.ic_unstarred_black
        override val color = R.color.swipe_action_custom
    }
}