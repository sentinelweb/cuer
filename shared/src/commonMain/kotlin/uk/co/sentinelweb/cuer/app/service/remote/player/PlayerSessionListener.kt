package uk.co.sentinelweb.cuer.app.service.remote.player

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract

class PlayerSessionListener(
    private val coroutines: CoroutineContextProvider,
    private val mapper: PlayerMessageToIntentMapper,
    private val log: LogWrapper,
) : PlayerSessionContract.Listener {

    init {
        log.tag(this)
    }

    private val _intentFlow = MutableSharedFlow<Intent>()
    val intentFlow: Flow<Intent> = _intentFlow

    override fun messageRecieved(message: PlayerSessionContract.PlayerCommandMessage) {
        log.d("messageRecieved: $message")
        coroutines.mainScope.launch {
            _intentFlow.emit(mapper.map(message))
        }
    }
}