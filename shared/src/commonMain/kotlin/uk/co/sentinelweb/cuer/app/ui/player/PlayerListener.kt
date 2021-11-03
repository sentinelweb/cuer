package uk.co.sentinelweb.cuer.app.ui.player

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent.*
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PlayerListener constructor(
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
) : PlayerContract.PlayerControls.Listener {

    init {
        log.tag(this)
    }

    private val _intentFlow = MutableSharedFlow<Intent>()
    val intentFlow: Flow<Intent>
        get() = _intentFlow

    override fun play() {
        coroutines.mainScope.launch {
            log.d("play: ")
            _intentFlow.emit(PlayPause(null))
        }
    }

    override fun pause() {
        coroutines.mainScope.launch {
            log.d("pause: ")
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
            val value = SeekToPosition(positionMs)
            log.d("seek: $value")
            _intentFlow.emit(value)
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
            log.d("SkipBack: ")
            _intentFlow.emit(SkipBack)
        }
    }

    override fun skipFwd() {
        coroutines.mainScope.launch {
            log.d("SkipFwd: ")
            _intentFlow.emit(SkipFwd)
        }
    }

}