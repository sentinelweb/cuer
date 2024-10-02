package uk.co.sentinelweb.cuer.app.ui.player

import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonCreator
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionMapper
import uk.co.sentinelweb.cuer.core.mappers.Format.SECS
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SHUFFLE
import uk.co.sentinelweb.cuer.domain.ext.isLiveOrUpcoming

class PlayerModelMapper constructor(
    private val timeFormatter: TimeFormatter,
    private val timeProvider: TimeProvider,
    private val descriptionMapper: DescriptionMapper,
    private val log: LogWrapper,
    private val ribbonCreator: RibbonCreator,
) {

    init {
        log.tag(this)
    }

    fun map(state: PlayerContract.MviStore.State): PlayerContract.View.Model =
        state.run {
            PlayerContract.View.Model(
                texts = PlayerContract.View.Model.Texts(
                    title = item?.media?.title ?: "No title",
                    playlistTitle = playlist?.title ?: "No playlist",
                    playlistData = playlist?.let { "${it.currentIndex + 1}/${it.items.size}" }
                        ?: "",
                    nextTrackText = trackTitle(playlist, 1),
                    lastTrackText = trackTitle(playlist, -1),
                    skipFwdText = skipFwdText,
                    skipBackText = skipBackText,
                    volumeText = volume.toInt().toString(),
                ),
                description = item?.media
                    ?.let {
                        descriptionMapper.map(
                            it,
                            playlist?.let { p -> setOf(p) },
                            false,
                            ribbonCreator.createPlayerRibbon(it)
                        )
                    }
                    ?: descriptionMapper.mapEmpty(),
                buttons = PlayerContract.View.Model.Buttons(
                    nextTrackEnabled = playlist?.run { currentIndex < playlist.items.size - 1 } ?: false,
                    prevTrackEnabled = playlist?.run { currentIndex > 0 } ?: false,
                    seekEnabled = item?.media?.duration != null && !item.media.isLiveOrUpcoming(),
                ),
                itemImage = item?.media?.thumbNail?.url,
                playState = playerState,
                times = PlayerContract.View.Model.Times(
                    positionText = mapPosition(),
                    durationText = item?.media?.duration?.let { timeFormatter.formatMillis(it, SECS) } ?: "--:--",
                    liveTime = mapLiveTime(),
                    isLive = item?.media?.isLiveOrUpcoming() ?: false,
                    seekBarFraction = item?.media?.positon?.let { pos ->
                        item.media.duration?.let { pos.toFloat() / it }
                    } ?: 0f
                ),
                content = content,
                playlistItem = state.item,
                playlistAndItem = state.playlistAndItem(),
                volume = state.volume
            )
        }
    //.also { log.d("buttons: ${it.buttons} index: ${state.playlist?.currentIndex} dur:${state.item?.media?.duration}") }

    private fun PlayerContract.MviStore.State.mapPosition() = item?.media?.let {
        if (it.isLiveBroadcast) {
            "-" + if (position > 0) timeFormatter.formatTime(position / 1000f) else ""
        } else {
            it.positon?.let { timeFormatter.formatMillis(it, SECS) } ?: "-"
        }
    } ?: "-"

    private fun PlayerContract.MviStore.State.mapLiveTime() = item?.media?.let {
        if (it.isLiveBroadcast) {
            val liveTime = timeProvider.getOffsetTime(position)
            val formatTime = timeFormatter.formatTime(liveTime)
            formatTime
        } else {
            null
        }
    }

    private fun trackTitle(playlist: PlaylistDomain?, offset: Int): String =
        playlist
            ?.takeIf { it.mode != SHUFFLE }
            ?.currentIndex
            ?.let { it + offset }
            ?.takeIf { it >= 0 && it < playlist.items.size }
            ?.let { playlist.items.get(it).media.title }
            ?: ""

}