package uk.co.sentinelweb.cuer.app.ui.resources

import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources

class NewPlaylistCustomisationResources : AppPlaylistInteractor.CustomisationResources {

    override val customDelete = ActionResources(
        label = "Mark watched",//res.getString(R.string.action_mark_watched),
        icon = 1,//R.drawable.ic_visibility_24,
        color = 1,//R.color.swipe_action_custom,
    )
}
