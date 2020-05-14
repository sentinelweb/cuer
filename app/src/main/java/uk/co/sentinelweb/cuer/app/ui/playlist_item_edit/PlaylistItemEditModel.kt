package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST_SELECT

data class PlaylistItemEditModel constructor(
    val imageUrl: String?,
    val title: String?,
    val description: String?,
    val chips: List<ChipModel> = listOf(
        ChipModel(PLAYLIST_SELECT)
    ),
    val channelTitle: String?,
    val channelThumbUrl: String?,
    val starred: Boolean,
    val canPlay: Boolean
)