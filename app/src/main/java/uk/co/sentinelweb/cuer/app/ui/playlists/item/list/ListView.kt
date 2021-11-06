package uk.co.sentinelweb.cuer.app.ui.playlists.item.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import uk.co.sentinelweb.cuer.app.databinding.ViewPlaylistsItemListBinding
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.tile.ItemTileView

class ListView : ItemContract.ListView {

    private lateinit var _binding: ViewPlaylistsItemListBinding
    private lateinit var presenter: ItemContract.ListPresenter

    override val parent: ViewGroup
        get() = _binding.list
    override val root: View
        get() = _binding.root

    fun init(parent: ViewGroup) {
        _binding =
            ViewPlaylistsItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun clear() {
        (0 .. _binding.list.childCount - 1).forEach {
            _binding.list.getChildAt(it).isVisible = false
        }
    }

    override fun setPresenter(listPresenter: ItemContract.ListPresenter) {
        presenter = listPresenter
    }
}
