package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import androidx.annotation.ColorRes
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST_SELECT

data class PlaylistItemEditModel constructor(
    val imageUrl: String?,
    val title: CharSequence?,
    val description: String?,
    val chips: List<ChipModel> = listOf(
        ChipModel(PLAYLIST_SELECT)
    ),
    val channelTitle: String?,
    val channelThumbUrl: String?,
    val channelDescription: String?,
    val pubDate: String?,
    val durationText: String?,
    val positionText: String?,
    val position: Float?,
    val starred: Boolean,
    val canPlay: Boolean,
    val empty: Boolean,
    val isLive: Boolean,
    val isUpcoming: Boolean,
    @ColorRes val infoTextBackgroundColor: Int
)