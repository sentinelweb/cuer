package uk.co.sentinelweb.cuer.app.ui.common.itemlist

import android.util.Log
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemModel

class ItemListPresenter constructor(
       private val view: ItemListContract.View,
       private val state : ItemListState
) : ItemListContract.Presenter {

    private lateinit var interactions: ItemContract.Interactions

    override fun setInteractions(inter:ItemContract.Interactions) {
        interactions = inter
    }

    override fun bind(itemlist: List<ItemModel>) {
        view.show(itemlist.size > 0)
        itemlist.forEachIndexed{ idx, model ->
            val itemPresenter: ItemContract.Presenter
            if (state.listItems.size <= idx) {
                itemPresenter = view.addItem(interactions)
                state.listItems.add(itemPresenter)
            } else {
                itemPresenter = state.listItems.get(idx)
            }
            itemPresenter.update(model)
        }
        Log.e("ItemListPresenter", "bind list ${state.listItems.size} ${state.listItems.size}")
        val modelSize = itemlist.size
        while (state.listItems.size > modelSize) {
            state.listItems.removeAt(modelSize)
        }
        view.clearFrom(modelSize)
    }

}
