package uk.co.sentinelweb.klink.ui.common.itemlist

import android.util.Log
import uk.co.sentinelweb.klink.ui.common.itemlist.item.ItemContract
import uk.co.sentinelweb.klink.ui.common.itemlist.item.ItemModel

class ItemListPresenter constructor(
        val view: ItemListContract.View
) : ItemListContract.Presenter {

    private val state = ItemListState()
    private lateinit var interactions: ItemContract.Interactions

    override fun setInteractions(inter:ItemContract.Interactions) {
        interactions = inter
    }

    override fun bind(itemlist: List<ItemModel>) {
        view.show(itemlist.size > 0)
        itemlist.forEachIndexed({ idx, model ->
            var itemPresenter: ItemContract.Presenter
            if (state.listItem.size <= idx) {
                itemPresenter = view.addItem(interactions)
                state.listItem.add(itemPresenter)
            } else {
                itemPresenter = state.listItem.get(idx)
            }
            itemPresenter.update(model)
        })
        Log.e("ItemListPresenter", "bind list ${state.listItem.size} ${state.listItem.size}")
        val modelSize = itemlist.size
        while (state.listItem.size > modelSize) {
            state.listItem.removeAt(modelSize)
        }
        view.clearFrom(modelSize)
    }

}
