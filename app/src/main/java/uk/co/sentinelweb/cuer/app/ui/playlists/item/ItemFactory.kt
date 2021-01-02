package uk.co.sentinelweb.cuer.app.ui.playlists.item

import android.view.LayoutInflater
import android.view.ViewGroup
import uk.co.sentinelweb.cuer.app.R

class ItemFactory constructor(
    private val modelMapper: ItemModelMapper
) {

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
    ): ItemContract.External {
        val itemPresenter = ItemPresenter(view, interactions, ItemContract.State(), modelMapper)
        view.setPresenter(itemPresenter)
        return itemPresenter
    }

    private fun createView(parent: ViewGroup): ItemContract.View {
        val inflate = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_playlists_item, parent, false)
        return inflate as ItemContract.View
    }

}
