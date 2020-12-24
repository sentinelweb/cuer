package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract

data class PlaylistModel constructor(
    val title: String,
    val imageUrl: String,
    val loopModeIndex: Int,
    @DrawableRes val loopModeIcon: Int,
    @DrawableRes val playIcon: Int,
    @DrawableRes val starredIcon: Int,
    val isDefault: Boolean,
    val items: List<ItemContract.PlaylistItemModel>
)