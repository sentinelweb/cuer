package uk.co.sentinelweb.cuer.app.ui.playlists.item.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import uk.co.sentinelweb.cuer.app.databinding.ViewPlaylistsItemListBinding
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.tile.ItemTileView

class ListView:ItemContract.ListView {

    private lateinit var _binding: ViewPlaylistsItemListBinding
    private lateinit var presenter: ItemContract.ListPresenter

    val root: View
        get() = _binding.root

    fun init(parent: ViewGroup) {
        _binding =
            ViewPlaylistsItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    fun clear() {
        _binding.list.removeAllViews()
    }

    fun addView(view: ItemTileView) {
        _binding.list.addView(view.root)
    }

    override fun setPresenter(listPresenter:ItemContract.ListPresenter) {
        presenter = listPresenter
    }
}
