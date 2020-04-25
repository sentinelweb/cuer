package uk.co.sentinelweb.cuer.app.ui.common.itemlist

import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemContract

data class ItemListState constructor(
    val listItem:MutableList<ItemContract.Presenter> = mutableListOf()
) : ViewModel()
