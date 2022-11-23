package uk.co.sentinelweb.cuer.app.ui.resources

import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources

class UnfinishedPlaylistCustomisationResources(

) : AppPlaylistInteractor.CustomisationResources {
    override val customDelete = ActionResources(
        label = "Mark complete",//res.getString(R.string.action_mark_complete),
        icon = 3,//R.drawable.ic_button_tick_24_white,
        color = 3//R.color.swipe_action_custom,
    )
}