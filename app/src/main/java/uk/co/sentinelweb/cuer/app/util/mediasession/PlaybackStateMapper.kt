package uk.co.sentinelweb.cuer.app.util.mediasession

import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

class PlaybackStateMapper {

    @Suppress("RemoveRedundantQualifierName")
    fun map(domain: MediaDomain?, state: PlayerStateDomain): PlaybackStateCompat =
        PlaybackStateCompat.Builder()
            .setState(mapState(state), domain?.positon ?: 0, 1f)
            .build()

    private fun mapState(state: PlayerStateDomain) = when (state) {
        PlayerStateDomain.UNKNOWN -> STATE_NONE
        PlayerStateDomain.UNSTARTED -> STATE_NONE
        PlayerStateDomain.ENDED -> STATE_STOPPED
        PlayerStateDomain.PLAYING -> STATE_PLAYING
        PlayerStateDomain.PAUSED -> STATE_PAUSED
        PlayerStateDomain.BUFFERING -> STATE_BUFFERING
        PlayerStateDomain.VIDEO_CUED -> STATE_BUFFERING
        PlayerStateDomain.ERROR -> STATE_ERROR
    }

    // available states
//    public static final int STATE_BUFFERING = 6;
//    public static final int STATE_CONNECTING = 8;
//    public static final int STATE_ERROR = 7;
//    public static final int STATE_FAST_FORWARDING = 4;
//    public static final int STATE_NONE = 0;
//    public static final int STATE_PAUSED = 2;
//    public static final int STATE_PLAYING = 3;
//    public static final int STATE_REWINDING = 5;
//    public static final int STATE_SKIPPING_TO_NEXT = 10;
//    public static final int STATE_SKIPPING_TO_PREVIOUS = 9;
//    public static final int STATE_SKIPPING_TO_QUEUE_ITEM = 11;
//    public static final int STATE_STOPPED = 1;
}