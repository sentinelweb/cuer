package uk.co.sentinelweb.cuer.app.ui.upcoming

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface UpcomingContract {

    interface Presenter {
        fun checkForUpcomingEpisodes(withinFutureMins: Int)
    }

    interface View {
        fun showNotification(item: PlaylistItemDomain)
    }

    data class Model(
        val items: List<Item>
    ) {
        data class Item(
            val episodeId: OrchestratorContract.Identifier<GUID>, // playlist Item
            val episodeTitle: String,
            val startTime: String,
            val startsIn: String
        )
    }

    companion object {
        const val UPCOMING_LIMIT_MINS = 30
    }
}