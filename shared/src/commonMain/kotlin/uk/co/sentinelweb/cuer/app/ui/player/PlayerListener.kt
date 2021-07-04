package uk.co.sentinelweb.cuer.app.ui.player

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent.*
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider

class PlayerListener constructor(
    private val coroutines: CoroutineContextProvider,
) : PlayerContract.PlayerControls.Listener {

    private val _intentFlow = MutableSharedFlow<Intent>()
    val intentFlow = _intentFlow

    override fun play() {
        coroutines.mainScope.launch {
            _intentFlow.emit(PlayPause(null))
        }
    }

    override fun pause() {
        coroutines.mainScope.launch {
            _intentFlow.emit(PlayPause(null))
        }
    }

    override fun trackBack() {
        coroutines.mainScope.launch {
            _intentFlow.emit(TrackBack)
        }
    }

    override fun trackFwd() {
        coroutines.mainScope.launch {
            _intentFlow.emit(TrackFwd)
        }
    }

    override fun seekTo(positionMs: Long) {
        coroutines.mainScope.launch {
            _intentFlow.emit(SeekToPosition(positionMs))
        }
    }

    override fun getLiveOffsetMs(): Long {
        // fixme not sure this is even used properly - consider remove
//        coroutines.mainScope.launch {
//            _intentFlow.emit(SeekToPosition(positionMs))
//        }
        return 0
    }

    override fun skipBack() {
        coroutines.mainScope.launch {
            _intentFlow.emit(SkipBack)
        }
    }

    override fun skipFwd() {
        coroutines.mainScope.launch {
            _intentFlow.emit(SkipBack)
        }
    }

}