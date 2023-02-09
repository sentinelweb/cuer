package uk.co.sentinelweb.cuer.app.ui.resources

import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources

class StarredPlaylistCustomisationResources : AppPlaylistInteractor.CustomisationResources {
    override val customDelete = ActionResources(
        label = "Unstar",//res.getString(R.string.menu_unstar),
        icon = 2,//R.drawable.ic_unstarred_black,
        color = 2,//R.color.swipe_action_custom,
    )
}
