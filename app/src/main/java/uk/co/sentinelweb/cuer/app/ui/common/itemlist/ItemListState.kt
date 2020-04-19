package uk.co.sentinelweb.klink.ui.common.itemlist

import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.klink.ui.common.itemlist.item.ItemContract

class ItemListState:ViewModel() {
    val listItem:MutableList<ItemContract.Presenter> = mutableListOf()
}
