package uk.co.sentinelweb.cuer.app.util.android_yt_player.live

import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences

interface LivePlaybackContract {

    interface Controller {
        fun setCurrentPosition(ms: Long)
        fun gotDuration(durationMs: Long)
        fun gotVideoId(id: String)
        fun getLiveOffsetMs(): Long
        fun clear(id: String)
    }

    interface PrefKeys {
        val durationValue: GeneralPreferences
        val durationObtainedTime: GeneralPreferences
        val durationVideoId: GeneralPreferences
    }

    data class State constructor(
        var positionMs: Long = 0,
        var durationMs: Long = -1,
        var durationObtainedTime: Long = -1,
        var receivedVideoId: String? = null
    )
}