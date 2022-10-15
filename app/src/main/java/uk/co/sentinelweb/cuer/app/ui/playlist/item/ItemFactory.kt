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
        cards: Boolean,
        interactions: ItemContract.Interactions
    ): ItemViewHolder {
        val createView = createView(parent, cards) // todo pass from pref
        return ItemViewHolder(
            createPresenter(createView, interactions),
            createView
        )
    }

    fun createPresenter(
        view: ItemContract.View,
        interactions: ItemContract.Interactions,
    ): ItemContract.External {
        val itemPresenter = ItemPresenter(
            view,
            interactions,
            ItemContract.State(),
            ItemTextMapper(res, iconMapper),
            ytContext
        )
        view.setPresenter(itemPresenter)
        return itemPresenter
    }

    fun createView(parent: ViewGroup, card: Boolean): ItemContract.View {
        val inflate = LayoutInflater.from(parent.context)
            .inflate(if (card) R.layout.view_playlist_card else R.layout.view_playlist_row, parent, false)
        return inflate as ItemContract.View
    }

}
