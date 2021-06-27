package uk.co.sentinelweb.cuer.app.ui.common.skip

import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SkipPresenter constructor(
    private val view: SkipContract.View,
    private val state: SkipContract.State,
    private val mapper: SkipContract.Mapper,
    private val log: LogWrapper,
    private val prefsWrapper: GeneralPreferencesWrapper
) : SkipContract.Presenter, SkipContract.External {

    override val skipBackText: String get() = mapper.mapBackTime(state.backJumpInterval.toLong())
    override val skipForwardText: String get() = mapper.mapForwardTime(state.forwardJumpInterval.toLong())

    override var duration: Long
        get() = state.duration
        set(value) {
            state.duration = value
        }

    override lateinit var listener: SkipContract.Listener

    private val isSeeking: Boolean
        get() = state.targetPosition != null && state.currentPlayState == BUFFERING // todo check
    override val skipForwardInterval: Int
        get() = state.forwardJumpInterval
    override val skipBackInterval: Int
        get() = state.backJumpInterval


    init {
        log.tag(this)
        updateSkipTimes()
    }

    override fun updateSkipTimes() {
        state.forwardJumpInterval = prefsWrapper.getInt(GeneralPreferences.SKIP_FWD_TIME, 30000)
        state.backJumpInterval = prefsWrapper.getInt(GeneralPreferences.SKIP_BACK_TIME, 30000)
    }

    override fun skipFwd() {
        state.accumulator += skipForwardInterval
        //log.d("skipFwd: accum=$accumulator isSeeking=$isSeeking")
        if (!isSeeking) {
            sendAccumulation()
        } else {
            sendAccumChange()
        }
    }

    override fun skipBack() {
        state.accumulator -= skipBackInterval
        //log.d("skipBack: accum=$accumulator isSeeking=$isSeeking")
        if (!isSeeking) {
            sendAccumulation()
        } else {
            sendAccumChange()
        }
    }

    private fun sendAccumChange() {
        val value = mapper.mapAccumulationTime(state.accumulator)
        //log.d("sendAccumChange: accum=$accumulator time=$value")
        onSkipAccumulationChange(
            value,
            if (state.accumulator == 0L) null else state.accumulator > 0
        )
    }

    override fun updatePosition(ms: Long) {
        state.position = ms
        state.targetPosition = state.targetPosition?.let {
            val targetFound = abs(ms - it) < 10000
            if (targetFound) null else it
        }
        if (state.targetPosition == null) {
            if (state.accumulator != 0L) {
                sendAccumulation()
            }
        }
    }

    override fun stateChange(playState: PlayerStateDomain) {
        if ((state.currentPlayState == BUFFERING || state.currentPlayState == PAUSED) && playState == PLAYING) {
            state.targetPosition = null
        }
        state.currentPlayState = playState
    }

    override fun onSelectSkipTime(fwd: Boolean) {
        view.showDialog(
            mapper.mapTimeSelectionDialogModel(
                if (fwd) state.forwardJumpInterval else state.backJumpInterval,
                fwd,
                { timeSelected -> onSkipTimeSelected(timeSelected, fwd) }
            )
        )
    }

    private fun sendAccumulation() {
        //log.d("sendAccumulation: accum=$accumulator targetPosition=$targetPosition")
        if (state.accumulator != 0L && state.targetPosition == null) {
            state.targetPosition = (state.position + state.accumulator).apply {
                onSkipAvailable(this)
            }
            state.accumulator = 0
            onSkipAccumulationChange("", null)
        }
    }

    private fun onSkipAvailable(target: Long) {
        if (target != 0L) {
            if (duration > 0) {
                val mx = max(0L, target)
                val mn: Long = min(duration, mx)
                listener.skipSeekTo(mn)
            }
        }
    }

    private fun onSkipAccumulationChange(value: String, fwd: Boolean?) {
        if (fwd == null) {
            listener.skipSetBackText(skipBackText)
            listener.skipSetFwdText(skipForwardText)
        } else if (fwd) {
            listener.skipSetBackText(skipBackText)
            listener.skipSetFwdText(value)
        } else {
            listener.skipSetBackText(value)
            listener.skipSetFwdText(skipForwardText)
        }
    }

    private fun onSkipTimeSelected(time: Int, fwd: Boolean) {
        if (fwd) {
            state.forwardJumpInterval = time
            prefsWrapper.putInt(GeneralPreferences.SKIP_FWD_TIME, time)
            listener.skipSetFwdText(skipForwardText)
        } else {
            state.backJumpInterval = time
            prefsWrapper.putInt(GeneralPreferences.SKIP_BACK_TIME, time)
            listener.skipSetBackText(skipBackText)
        }
    }

}