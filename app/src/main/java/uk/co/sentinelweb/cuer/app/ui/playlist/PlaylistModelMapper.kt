package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.mapper.BackgroundMapper
import uk.co.sentinelweb.cuer.app.ui.common.mapper.LoopModeMapper
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import java.time.OffsetDateTime


class PlaylistModelMapper constructor(
    private val res: ResourceWrapper,
    private val timeSinceFormatter: TimeSinceFormatter,
    private val timeFormatter: TimeFormatter,
    private val loopModeMapper: LoopModeMapper,
    private val backgroundMapper: BackgroundMapper
) {

    fun map(
        domain: PlaylistDomain,
        isPlaying: Boolean,
        mapItems: Boolean = true,
        id: OrchestratorContract.Identifier<*>
    ): PlaylistContract.Model = PlaylistContract.Model(
        domain.title,
        domain.image?.url ?: "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg",
        domain.mode.ordinal,
        loopModeMapper.mapIcon(domain.mode),
        if (isPlaying) R.drawable.ic_baseline_playlist_close_24 else R.drawable.ic_baseline_playlist_play_24,
        if (domain.starred) R.drawable.ic_button_starred_white else R.drawable.ic_button_unstarred_white,
        domain.default,
        id.source == LOCAL,
        id.source == LOCAL,
        if (mapItems) {
            domain.items.mapIndexed { index, item -> map(item, index) }
        } else {
            null
        }
    )

    private fun map(item: PlaylistItemDomain, index: Int): ItemContract.Model {
        val top = item.media.title ?: "No title"
        val pos = item.media.positon?.toFloat() ?: 0f
        val progress = item.media.duration?.let { pos / it.toFloat() } ?: 0f
        return ItemContract.Model(
            item.id!!,
            index = index,
            url = item.media.url,
            type = item.media.mediaType,
            title = top,
            duration = (
                    if (item.media.isLiveBroadcast) {
                        if (item.media.isLiveBroadcastUpcoming) res.getString(R.string.upcoming)
                        else res.getString(R.string.live)
                    } else (item.media.duration?.let { item.media.duration?.let { timeFormatter.formatMillis(it) } } ?: "-")
                    ),
            positon = if (item.media.isLiveBroadcast) res.getString(R.string.live) else (progress * 100).toInt().toString() + "%",
            thumbNailUrl = item.media.thumbNail?.url,
            progress = progress,
            starred = item.media.starred,
            watchedSince = item.media.dateLastPlayed?.let { timeSinceFormatter.formatTimeSince(it.toEpochMilli()) } ?: "-",
            isWatched = item.media.watched,
            published = item.media.published?.let {
                timeSinceFormatter.formatTimeSince(
                    it.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli()
                )
            } ?: "-",
            platform = item.media.platform,
            isLive = item.media.isLiveBroadcast,
            isUpcoming = item.media.isLiveBroadcastUpcoming,
            infoTextBackgroundColor = backgroundMapper.mapInfoBackground(item.media)
        )
    }

    fun mapChangePlaylistAlert(confirm: () -> Unit, info: () -> Unit): AlertDialogModel = AlertDialogModel(
        R.string.playlist_change_dialog_title,
        R.string.playlist_change_dialog_message,
        AlertDialogModel.Button(R.string.ok, confirm),
        AlertDialogModel.Button(R.string.dialog_button_view_info, info)
    )

}
