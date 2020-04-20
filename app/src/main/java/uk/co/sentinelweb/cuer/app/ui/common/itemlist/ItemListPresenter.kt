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
            if (state.listItem.size <= idx) {
                itemPresenter = view.addItem(interactions)
                state.listItem.add(itemPresenter)
            } else {
                itemPresenter = state.listItem.get(idx)
            }
            itemPresenter.update(model)
        }
        Log.e("ItemListPresenter", "bind list ${state.listItem.size} ${state.listItem.size}")
        val modelSize = itemlist.size
        while (state.listItem.size > modelSize) {
            state.listItem.removeAt(modelSize)
        }
        view.clearFrom(modelSize)
    }

}
