package uk.co.sentinelweb.cuer.app.ui.player

import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionMapper
import uk.co.sentinelweb.cuer.core.mappers.Format.SECS
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SHUFFLE

class PlayerModelMapper constructor(
    private val timeFormatter: TimeFormatter,
    private val descriptionMapper: DescriptionMapper
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
                description = item?.media
                    ?.let { descriptionMapper.map(it, playlist?.let { setOf(it) }, false) }
                    ?: descriptionMapper.mapEmpty(),
                nextTrackEnabled = playlist?.run { currentIndex < playlist.items.size - 1 } ?: false,
                prevTrackEnabled = playlist?.run { currentIndex > 0 } ?: false,
                itemImage = item?.media?.thumbNail?.url,
                playState = playerState,
                times = PlayerContract.View.Model.Times(
                    positionText = mapPosition(),
                    durationText = item?.media?.duration?.let { timeFormatter.formatMillis(it, SECS) } ?: "-",
                    isLive = item?.media?.run { isLiveBroadcast || isLiveBroadcastUpcoming } ?: false,
                    seekBarFraction = item?.media?.positon?.let { pos ->
                        item.media.duration?.let { pos.toFloat() / it }
                    } ?: 0f
                ),
                screen = screen
            )
        }

    private fun PlayerContract.MviStore.State.mapPosition() = item?.media?.let {
        if (it.isLiveBroadcast) {
            "-" + if (position > 0) timeFormatter.formatTime(position / 1000f) else ""
        } else {
            it.positon?.let { timeFormatter.formatMillis(it, SECS) } ?: "-"
        }
    } ?: "-"

    private fun trackTitle(playlist: PlaylistDomain?, offset: Int): String =
        playlist
            ?.takeIf { it.mode != SHUFFLE }
            ?.currentIndex
            ?.let { it + offset }
            ?.takeIf { it >= 0 && it < playlist.items.size }
            ?.let { playlist.items.get(it).media.title }
            ?: ""

}