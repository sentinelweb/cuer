package uk.co.sentinelweb.klink.ui.common.itemlist

import uk.co.sentinelweb.klink.ui.common.itemlist.item.ItemContract
import uk.co.sentinelweb.klink.ui.common.itemlist.item.ItemModel

interface ItemListContract {

    interface View {
        fun addItem(interactions: ItemContract.Interactions): ItemContract.Presenter
        fun clearFrom(index:Int)
        fun show(b: Boolean)

    }

    interface Presenter {
        fun bind(itemlist:List<ItemModel>)
        fun setInteractions(inter: ItemContract.Interactions)
    }
}
