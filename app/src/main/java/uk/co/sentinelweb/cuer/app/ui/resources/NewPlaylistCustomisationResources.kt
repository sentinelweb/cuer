package uk.co.sentinelweb.cuer.app.ui.resources

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class NewPlaylistCustomisationResources(
    private val res: ResourceWrapper
) : AppPlaylistInteractor.CustomisationResources {

    override val customDelete = ActionResources(
        label = res.getString(R.string.action_mark_watched),
        icon = R.drawable.ic_visibility_24,
        color = R.color.swipe_action_custom,
    )
}