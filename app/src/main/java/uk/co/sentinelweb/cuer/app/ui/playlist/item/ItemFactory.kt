package uk.co.sentinelweb.cuer.app.ui.playlist.item

import android.view.LayoutInflater
import android.view.ViewGroup
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class ItemFactory constructor(
    private val res: ResourceWrapper,
    private val iconMapper: IconMapper,
    private val ytContext: ChromecastYouTubePlayerContextHolder
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
        interactions: ItemContract.Interactions,
    ): ItemContract.External {
        val itemPresenter = ItemPresenter(view, interactions, ItemContract.State(), ItemModelMapper(res, iconMapper), ytContext)
        view.setPresenter(itemPresenter)
        return itemPresenter
    }

    private fun createView(parent: ViewGroup): ItemContract.View {
        val inflate = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_playlist_item, parent, false)
        return inflate as ItemContract.View
    }

}
