package uk.co.sentinelweb.cuer.app.ui.remotes

import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.NodeDomain

class RemotesModelMapper constructor(
    private val strings: StringDecoder,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    fun map(state: RemotesContract.MviStore.State): Model {
        return Model(
            title = "Remotes",
            imageUrl = "https://cuer-275020.firebaseapp.com/images/headers/remotes..png",
            nodes = state.nodes.map { mapNode(it) }
        ).also { log.d("mapped: $it") }
    }

    private fun mapNode(it: NodeDomain) =
        RemotesContract.View.NodeModel(
            id = it.id,
            title = it.hostname ?: "No hostname",
        )

}
