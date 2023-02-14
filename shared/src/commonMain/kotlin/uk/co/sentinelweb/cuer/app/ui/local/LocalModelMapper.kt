package uk.co.sentinelweb.cuer.app.ui.local

import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class LocalModelMapper constructor(
    private val strings: StringDecoder,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    fun map(state: LocalContract.MviStore.State): Model {
        return Model(
            title = "Configure Host",
            imageUrl = "https://cuer-275020.firebaseapp.com/images/headers/artificial-intelligence-3382507_640.jpg",
            localNodeDomain = state.localNode,
            serverState = state.serverState,
            address = state.serverAddress,
        )
    }
}
