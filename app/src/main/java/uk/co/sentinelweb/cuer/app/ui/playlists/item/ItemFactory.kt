package uk.co.sentinelweb.cuer.app.ui.playlists.item

import android.view.ViewGroup
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.header.HeaderView
import uk.co.sentinelweb.cuer.app.ui.playlists.item.header.HeaderViewHolder
import uk.co.sentinelweb.cuer.app.ui.playlists.item.list.ListPresenter
import uk.co.sentinelweb.cuer.app.ui.playlists.item.list.ListView
import uk.co.sentinelweb.cuer.app.ui.playlists.item.list.ListViewHolder
import uk.co.sentinelweb.cuer.app.ui.playlists.item.row.ItemRowView
import uk.co.sentinelweb.cuer.app.ui.playlists.item.row.ItemRowViewHolder
import uk.co.sentinelweb.cuer.app.ui.playlists.item.tile.ItemTileView

class ItemFactory constructor(
    private val modelMapper: ItemModelMapper
) {

    fun createItemViewHolder(
        parent: ViewGroup,
        interactions: ItemContract.Interactions
    ): ItemRowViewHolder {
        val createView = createRowView(parent)
        return ItemRowViewHolder(
            createPresenter(createView, interactions),
            createView as ItemRowView
        )
    }

    fun createPresenter(
        view: ItemContract.View,
        interactions: ItemContract.Interactions
    ): ItemContract.External<PlaylistsItemMviContract.Model.Item> {
        val itemPresenter = ItemPresenter(view, interactions, ItemContract.State(), modelMapper)
        view.setPresenter(itemPresenter)
        return itemPresenter
    }

    private fun createRowView(parent: ViewGroup): ItemContract.View {
        val inflate = ItemRowView()
        inflate.init(parent)
        return inflate
    }

    fun createTileView(parent: ViewGroup): ItemContract.View {
        val inflate = ItemTileView()
        inflate.init(parent)
        return inflate
    }

    fun createHeaderViewHolder(parent: ViewGroup):HeaderViewHolder  =
        HeaderViewHolder(
            HeaderView().apply { init(parent) }
        )

    fun createListViewHolder(parent: ViewGroup, interactions: ItemContract.Interactions):ListViewHolder {
        val listView = ListView().apply { init(parent) }
        val listPresenter = ListPresenter(ItemContract.ListState(),listView, this, interactions)
        listView.setPresenter(listPresenter)
        return ListViewHolder(
            listView,
            listPresenter
        )
    }
}
