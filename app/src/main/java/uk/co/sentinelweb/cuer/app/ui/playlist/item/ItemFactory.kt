package uk.co.sentinelweb.cuer.app.ui.playlist.item

import android.view.LayoutInflater
import android.view.ViewGroup
import uk.co.sentinelweb.cuer.app.R

class ItemFactory {

    fun createItemViewHolder(
        parent: ViewGroup,
        interactions: ItemContract.Interactions
    ): ItemViewHolder {
        val createView = createView(parent)
        return ItemViewHolder(
            createPresenter(createView, interactions),
            createView as ItemView
        )
    }

    private fun createPresenter(
        view: ItemContract.View,
        interactions: ItemContract.Interactions
    ): ItemContract.Presenter {
        val itemPresenter = ItemPresenter(view, interactions, ItemState())
        view.setPresenter(itemPresenter)
        return itemPresenter
    }

    private fun createView(parent: ViewGroup): ItemContract.View {
        val inflate = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_playlist_item, parent, false)
        return inflate as ItemContract.View
    }

}
