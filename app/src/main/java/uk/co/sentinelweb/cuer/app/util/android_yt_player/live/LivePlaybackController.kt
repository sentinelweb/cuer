package uk.co.sentinelweb.cuer.app.util.android_yt_player.live

import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class LivePlaybackController constructor(
    private val state: LivePlaybackContract.State,
    private val prefKeys: LivePlaybackContract.PrefKeys,
    private val prefs: GeneralPreferencesWrapper,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper
) : LivePlaybackContract.Controller {

    override fun setCurrentPosition(sec: Float) {
        state.positionSec = sec
    }

    override fun getLiveOffsetMs(): Long {
        if (state.durationObtainedTime > -1) {
            val timeSinceDurationMs = timeProvider.currentTimeMillis() - state.durationObtainedTime
            val currentDurationSec = state.durationSec + (timeSinceDurationMs / 1000f)
            val offsetSec =
                currentDurationSec - state.positionSec - 3600 //  - timeProvider.timeZomeOffsetSecs() // some problem here 1 hour more (not timezone?)
            log.d("offsetSec:$offsetSec state.durationSec:${state.durationSec} state.durationObtainedTime:${state.durationObtainedTime}")
            return (offsetSec * 1000).toLong()
        }
        return -1
    }

    override fun gotDuration(duration: Float) {
        if (state.durationObtainedTime == -1L) {
            state.durationSec = duration
            state.durationObtainedTime = timeProvider.currentTimeMillis()
            state.receivedVideoId?.let { saveLiveDurationPref() }
        }
    }

    override fun clear(id: String) {
        state.durationObtainedTime = -1
        clearLiveDurationPrefIfNotSame(id)
    }

    private fun saveLiveDurationPref() {
        prefs.putLong(prefKeys.durationObtainedTime, state.durationObtainedTime)
        prefs.putString(prefKeys.durationVideoId, state.receivedVideoId ?: throw IllegalStateException("Should have id"))
        prefs.putLong(prefKeys.durationValue, state.durationSec.toLong())
    }

    override fun gotVideoId(id: String) {
        state.receivedVideoId = id
        if (prefs.getString(prefKeys.durationVideoId, null) == id) {
            state.durationSec = prefs.getLong(prefKeys.durationValue)?.toFloat() ?: 0f
            state.durationObtainedTime = prefs.getLong(prefKeys.durationObtainedTime) ?: -1
            log.d("restored duration")
        } else {
            log.d("did not restore")
        }
    }

    private fun clearLiveDurationPrefIfNotSame(id: String) {
        if (prefs.getString(prefKeys.durationVideoId, null) != id) {
            prefs.remove(prefKeys.durationValue)
            prefs.remove(prefKeys.durationObtainedTime)
            prefs.remove(prefKeys.durationVideoId)
            log.d("cleared duration")
        } else {
            log.d("did not clear duration")
        }
    }

}