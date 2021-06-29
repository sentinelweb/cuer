package uk.co.sentinelweb.cuer.app.ui.player

import uk.co.sentinelweb.cuer.core.mappers.Format.SECS
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.domain.PlaylistDomain


class PlayerModelMapper constructor(
    private val timeSinceFormatter: TimeSinceFormatter,
    private val timeFormatter: TimeFormatter
) {
    fun map(state: PlayerContract.MviStore.State): PlayerContract.View.Model =
        state.run {
            PlayerContract.View.Model(
                texts = PlayerContract.View.Model.Texts(
                    title = item?.media?.title ?: "No title",
                    playlistTitle = playlist?.title ?: "No playlist",
                    playlistData = playlist?.let { "${it.currentIndex + 1}/${it.items.size}" } ?: "",
                    nextTrackText = trackTitle(playlist, 1),
                    lastTrackText = trackTitle(playlist, -1),
                    skipFwdText = skipFwdText,
                    skipBackText = skipBackText
                ),
                nextTrackEnabled = playlist?.run { currentIndex < playlist.items.size - 1 } ?: false,
                prevTrackEnabled = playlist?.run { currentIndex > 0 } ?: false,
                platformId = item?.media?.platformId,
                itemImage = item?.media?.thumbNail?.url,
                playState = playerState,
                times = PlayerContract.View.Model.Times(
                    positionText = item?.media?.positon?.let { timeFormatter.formatMillis(it, SECS) } ?: "-",
                    durationText = item?.media?.duration?.let { timeFormatter.formatMillis(it, SECS) } ?: "-",
                    isLive = item?.media?.run { isLiveBroadcast || isLiveBroadcastUpcoming } ?: false,
                    seekBarFraction = item?.media?.positon?.let { pos ->
                        item.media.duration?.let { pos.toFloat() / it }
                    } ?: 0f
                )
            )
        }

    private fun trackTitle(playlist: PlaylistDomain?, offset: Int): String =
        playlist?.currentIndex
            ?.let { it + offset }
            ?.takeIf { it >= 0 && it < playlist.items.size }
            ?.let { playlist.items.get(it).media.title }
            ?: ""

}