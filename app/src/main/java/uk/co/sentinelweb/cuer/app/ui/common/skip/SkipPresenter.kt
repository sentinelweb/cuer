package uk.co.sentinelweb.cuer.app.ui.common.skip

import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import kotlin.math.abs

class SkipPresenter constructor(
    private val timeSinceFormatter: TimeSinceFormatter,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper

) {
    var forwardJumpInterval: Long = 30000
    var backJumpInterval: Long = 30000
    var duration: Long = 0

    lateinit var listener: Listener

    private var accumulator: Long = 0
    private val isSeeking: Boolean
        get() = targetPosition != null

    private var targetPosition: Long? = null
    private var position: Long = 0
    private var currentStateState: PlayerStateDomain? = null

    init {
        log.tag(this)
    }

    val skipFwdDefaultText
        get() = timeSinceFormatter.formatTimeShort(forwardJumpInterval)
    val skipBackDefaultText
        get() = "-" + timeSinceFormatter.formatTimeShort(backJumpInterval)

    fun skipFwd() {
        accumulator += forwardJumpInterval
        //log.d("skipFwd: accum=$accumulator isSeeking=$isSeeking")
        if (!isSeeking) {
            sendAccumulation()
        } else {
            sendAccumChange()
        }
    }

    fun skipBack() {
        accumulator -= backJumpInterval
        //log.d("skipBack: accum=$accumulator isSeeking=$isSeeking")
        if (!isSeeking) {
            sendAccumulation()
        } else {
            sendAccumChange()
        }
    }

    private fun sendAccumChange() {
        val value = timeSinceFormatter.formatTimeShort(accumulator)
        //log.d("sendAccumChange: accum=$accumulator time=$value")
        onSkipAccumulationChange(
            value,
            if (accumulator == 0L) null else accumulator > 0
        )
    }

    fun updatePosition(ms: Long) {
        position = ms
        targetPosition = targetPosition?.let {
            val targetFound = abs(ms - it) < 10000
            if (targetFound) null else it
        }
        if (targetPosition == null) {
            if (accumulator != 0L) {
                sendAccumulation()
            }
        }
    }

    fun stateChange(playState: PlayerStateDomain) {
        if (currentStateState == PlayerStateDomain.BUFFERING && playState == PlayerStateDomain.PLAYING) {
            targetPosition = null
        }
        currentStateState = playState
    }

    private fun sendAccumulation() {
        //log.d("sendAccumulation: accum=$accumulator targetPosition=$targetPosition")
        if (accumulator != 0L && targetPosition == null) {
            targetPosition = (position + accumulator).apply {
                onSkipAvailable(this)
            }
            accumulator = 0
            onSkipAccumulationChange("", null)
        }
    }

    private fun onSkipAvailable(target: Long) {
        if (target != 0L) {
            if (duration > 0) {
                val mx = java.lang.Long.max(0L, target)
                val mn: Long = java.lang.Long.min(duration, mx)
                listener.skipSeekTo(mn)
            }

        }
    }

    private fun onSkipAccumulationChange(value: String, fwd: Boolean?) {
        if (fwd == null) {
            listener.skipSetBackText(skipBackDefaultText)
            listener.skipSetFwdText(skipFwdDefaultText)
        } else if (fwd) {
            listener.skipSetBackText(skipBackDefaultText)
            listener.skipSetFwdText(value)
        } else {
            listener.skipSetBackText(value)
            listener.skipSetFwdText(skipFwdDefaultText)
        }
    }

    interface Listener {
        fun skipSeekTo(target: Long)
        fun skipSetBackText(text: String)
        fun skipSetFwdText(text: String)
    }
}