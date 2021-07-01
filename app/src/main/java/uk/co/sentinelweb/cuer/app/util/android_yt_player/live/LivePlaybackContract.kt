package uk.co.sentinelweb.cuer.app.util.android_yt_player.live

import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences

interface LivePlaybackContract {

    interface Controller {
        fun setCurrentPosition(sec: Float)
        fun gotDuration(duration: Float)
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
        var positionSec: Float = 0f,
        var durationSec: Float = 0f,
        var durationObtainedTime: Long = -1,
        var receivedVideoId: String? = null
    )
}