package uk.co.sentinelweb.cuer.app.ui.common.itemlist

import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemContract

class ItemListState:ViewModel() {
    val listItem:MutableList<ItemContract.Presenter> = mutableListOf()
}
