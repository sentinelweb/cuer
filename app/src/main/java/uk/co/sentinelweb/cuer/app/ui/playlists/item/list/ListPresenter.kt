package uk.co.sentinelweb.cuer.app.ui.playlists.item.list

import android.view.ViewGroup
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract.Model.ListModel
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.tile.ItemTileView

class ListPresenter(
    private val state: ItemContract.ListState,
    private val listView: ItemContract.ListView,
    private val itemFactory: ItemFactory,
    private val interactions: ItemContract.Interactions
) : ItemContract.ListPresenter, ItemContract.External<ListModel> {

    override fun update(item: ListModel, current: OrchestratorContract.Identifier<*>?) {
        listView.clear()
        item.items.forEachIndexed { i, itemModel ->
            val itemPresenter = if (state.presenters.size <= i) {
                itemFactory.createTileView(listView.parent)
                    .let { itemFactory.createPresenter(it, interactions) to it }
                    .also { it.second.setPresenter(it.first as ItemContract.Presenter)}
                    .also { state.presenters.add(it.first) }
                    .first
            } else {
                state.presenters[i]
            }
            itemPresenter.update(itemModel, current)
        }
    }

    override fun doLeft() {}

    override fun doRight() {}

    override fun canDragLeft(): Boolean = false

    override fun canDragRight(): Boolean = false

    override fun canReorder(): Boolean = false
}