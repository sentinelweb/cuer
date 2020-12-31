package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import java.time.OffsetDateTime


class PlaylistModelMapper constructor(
    private val res: ResourceWrapper,
    private val timeSinceFormatter: TimeSinceFormatter,
    private val timeFormatter: TimeFormatter
) {

    fun map(domain: PlaylistDomain, isPlaying: Boolean, mapItems: Boolean = true): PlaylistContract.Model = PlaylistContract.Model(
        domain.title,
        domain.image?.url ?: "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg",
        domain.mode.ordinal,
        when (domain.mode) {
            SINGLE -> R.drawable.ic_button_shuffle_disabled_24
            LOOP -> R.drawable.ic_button_repeat_24
            SHUFFLE -> R.drawable.ic_button_shuffle_24
        },
        if (isPlaying) R.drawable.ic_player_pause_black else R.drawable.ic_button_play_black,
        if (domain.starred) R.drawable.ic_button_starred_white else R.drawable.ic_button_unstarred_white,
        domain.default,
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
            duration = item.media.duration?.let { timeFormatter.formatMillis(it) } ?: "-",
            positon = "" + (progress * 100).toInt() + "%",
            thumbNailUrl = item.media.thumbNail?.url,
            progress = progress,
            starred = item.media.starred,
            watched = item.media.dateLastPlayed?.let { timeSinceFormatter.formatTimeSince(it.toEpochMilli()) } ?: "-",
            published = item.media.published?.let {
                timeSinceFormatter.formatTimeSince(
                    it.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli()
                )
            } ?: "-"
        )
    }

    fun mapChangePlaylistAlert(confirm: () -> Unit): AlertDialogModel = AlertDialogModel(
        res.getString(R.string.playlist_change_dialog_title),
        res.getString(R.string.playlist_change_dialog_message),
        confirm
    )

}
