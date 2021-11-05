package uk.co.sentinelweb.cuer.app.ui.playlists.item.list

import android.view.ViewGroup
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.tile.ItemTileView

class ListPresenter(
    private val listView: ListView,
    private val itemFactory: ItemFactory,
    private val interactions: ItemContract.Interactions
) {

    fun update(model: ItemContract.Model.ListModel) {
        listView.clear()
        model.items
            .map { itemFactory.createTileView(listView.root as ViewGroup) }
            .onEach { listView.addView(it as ItemTileView) }
            .onEach {
                val itemPresenter =
                    itemFactory.createPresenter(it, interactions) as ItemContract.Presenter
                it.setPresenter(itemPresenter)
            }
    }
}