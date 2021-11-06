package uk.co.sentinelweb.cuer.app.ui.playlists.item.list

import android.view.ViewGroup
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract.Model.ListModel
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.tile.ItemTileView

class ListPresenter(
    private val listView: ListView,
    private val itemFactory: ItemFactory,
    private val interactions: ItemContract.Interactions
) : ItemContract.ListPresenter, ItemContract.External<ListModel> {

    override fun update(item: ListModel, current: OrchestratorContract.Identifier<*>?) {
        listView.clear()
        item.items
            .map { itemFactory.createTileView(listView.root as ViewGroup) to it }
            .onEach { listView.addView(it.first as ItemTileView) }
            .onEach {
                val itemPresenter =
                    itemFactory.createPresenter(it.first, interactions)
                it.first.setPresenter(itemPresenter as ItemContract.Presenter)
                itemPresenter.update(it.second, current)
            }
    }

    override fun doLeft() {}

    override fun doRight() {}

    override fun canDragLeft(): Boolean = false

    override fun canDragRight(): Boolean = false

    override fun canReorder(): Boolean = false
}