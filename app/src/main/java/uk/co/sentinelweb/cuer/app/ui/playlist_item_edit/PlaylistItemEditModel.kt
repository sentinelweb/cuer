package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import uk.co.sentinelweb.cuer.app.ui.common.ChipModel

data class PlaylistItemEditModel constructor(
    val imageUrl: String?,
    val title: String?,
    val description: String?,
    val chips: List<ChipModel>,
    val author: String?,
    val authorImgUrl: String?,
    val starred: Boolean,
    val canPlay: Boolean
)