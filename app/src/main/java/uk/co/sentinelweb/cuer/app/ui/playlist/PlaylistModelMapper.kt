package uk.co.sentinelweb.cuer.app.ui.playlist

import android.text.SpannableString
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain


class PlaylistModelMapper constructor(
    private val res: ResourceWrapper,
    private val timeSinceFormatter: TimeSinceFormatter,
    private val timeFormatter: TimeFormatter
) {

    fun map(domain: PlaylistDomain, isPlaying: Boolean, mapItems: Boolean = true): PlaylistModel = PlaylistModel(
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

    private fun map(item: PlaylistItemDomain, index: Int): ItemContract.PlaylistItemModel {
        val top = item.media.title ?: "No title"
        val pos = item.media.positon?.toFloat() ?: 0f
        val bottom = SpannableString("[star] [pos] [pub]/[wat]")
        //bottom.setSpan(ImageSpan(res.getDrawable(if (item.media.starred) R.drawable.ic_button_starred_white else R.drawable.ic_button_starred_white , R.color.col)), 7, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ItemContract.PlaylistItemModel(
            item.id!!,
            index = index,
            url = item.media.url,
            type = item.media.mediaType,
            title = top,
            length = item.media.duration?.let { "${it / 1000}s" } ?: "-",
            positon = item.media.positon?.let { "${it / 1000}s" } ?: "-",
            thumbNailUrl = item.media.thumbNail?.url,
            bottomText = bottom,
            progress = item.media.duration?.let { pos * it.toFloat() } ?: 0f
        )
    }

    fun mapChangePlaylistAlert(confirm: () -> Unit): AlertDialogModel = AlertDialogModel(
        res.getString(R.string.playlist_change_dialog_title),
        res.getString(R.string.playlist_change_dialog_message),
        confirm
    )

}
