package uk.co.sentinelweb.cuer.app.util.ayt_player.live

import uk.co.sentinelweb.cuer.app.util.android_yt_player.live.LivePlaybackContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

// todo move LivePlaybackController to shared move all prefs to MultiPlatformPreferencesWrapper
class LivePlaybackController(
    private val state: LivePlaybackContract.State,
    private val prefKeys: LivePlaybackContract.PrefKeys,
    private val prefs: GeneralPreferencesWrapper,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper
) : LivePlaybackContract.Controller {

    init {
        log.tag(this)
    }

    override fun setCurrentPosition(ms: Long) {
        state.positionMs = ms
    }

    override fun getLiveOffsetMs(): Long {
        if (state.durationObtainedTime > -1) {
            val timeSinceDurationMs = timeProvider.currentTimeMillis() - state.durationObtainedTime
            val currentDurationMs = state.durationMs + timeSinceDurationMs
            val offsetMs =
                currentDurationMs - state.positionMs - (3600 * 1000) //  - timeProvider.timeZomeOffsetSecs() // some problem here 1 hour more (not timezone?)
            log.d("getLiveOffsetMs: id: ${state.receivedVideoId} offsetSec:$offsetMs state.durationSec:${state.durationMs} state.durationObtainedTime:${state.durationObtainedTime}")
            return offsetMs
        }
        return -1
    }

    override fun gotDuration(durationMs: Long) {
        log.d("gotDuration: id: ${state.receivedVideoId} durationMs:$durationMs state.durationObtainedTime:${state.durationObtainedTime}")
        if (state.durationObtainedTime == -1L) {
            state.durationMs = durationMs
            state.durationObtainedTime = timeProvider.currentTimeMillis()
            state.receivedVideoId?.let { saveLiveDurationPref() }
        }
    }

    override fun clear(id: String) {
        state.durationObtainedTime = -1
        clearLiveDurationPrefIfNotSame(id)
    }

    override fun gotVideoId(id: String) {
        state.receivedVideoId = id
        if (prefs.getString(prefKeys.durationVideoId, null) == id) {
            state.durationMs = prefs.getLong(prefKeys.durationValue) ?: 0
            state.durationObtainedTime = prefs.getLong(prefKeys.durationObtainedTime) ?: -1
            log.d("restored duration")
        } else {
            log.d("did not restore")
        }
    }

    private fun saveLiveDurationPref() {
        prefs.putLong(prefKeys.durationObtainedTime, state.durationObtainedTime)
        prefs.putString(prefKeys.durationVideoId, state.receivedVideoId ?: throw IllegalStateException("Should have id"))
        prefs.putLong(prefKeys.durationValue, state.durationMs)
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
