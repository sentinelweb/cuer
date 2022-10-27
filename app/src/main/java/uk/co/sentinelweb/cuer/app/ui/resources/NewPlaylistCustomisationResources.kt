package uk.co.sentinelweb.cuer.app.ui.resources

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class NewPlaylistCustomisationResources(
    private val res: ResourceWrapper
) : AppPlaylistInteractor.CustomisationResources {

    override val customDelete = object : ActionResources {
        override val label = res.getString(R.string.action_mark_watched)
        override val icon = R.drawable.ic_visibility_24
        override val color = R.color.swipe_action_custom
    }
}