package uk.co.sentinelweb.cuer.app.ui.playlists.item.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import uk.co.sentinelweb.cuer.app.databinding.ViewItemHeaderBinding
import uk.co.sentinelweb.cuer.app.databinding.ViewPlaylistsItemListBinding
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.tile.ItemTileView

class ListView() {

    private lateinit var _binding: ViewPlaylistsItemListBinding
    private lateinit var presenter: ItemContract.Presenter

    val root: View
        get() = _binding.root

    fun init(parent: ViewGroup) {
        _binding =
            ViewPlaylistsItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    fun clear() {
        _binding.root.removeAllViews()
    }

    fun addView(view: ItemTileView) {
        _binding.root.addView(view.root)
    }
}
