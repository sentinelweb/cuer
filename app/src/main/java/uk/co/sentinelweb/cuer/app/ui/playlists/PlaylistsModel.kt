package uk.co.sentinelweb.cuer.app.ui.playlists

import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemModel

data class PlaylistsModel constructor(
    val imageUrl: String = "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg",
    val items: List<ItemModel>
) {

}