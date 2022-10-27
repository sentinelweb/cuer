package uk.co.sentinelweb.cuer.app.ui.resources

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class UnfinishedPlaylistCustomisationResources(
    private val res: ResourceWrapper
) : AppPlaylistInteractor.CustomisationResources {
    override val customDelete = object : ActionResources {
        override val label = res.getString(R.string.action_mark_complete)
        override val icon = R.drawable.ic_button_tick_24_white
        override val color = R.color.swipe_action_custom
    }
}