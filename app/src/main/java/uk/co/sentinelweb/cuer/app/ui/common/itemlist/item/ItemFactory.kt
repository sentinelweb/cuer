package uk.co.sentinelweb.cuer.app.ui.common.itemlist.item

import android.view.View
import android.view.ViewGroup

class ItemFactory {

    fun createPresenter(parent: ViewGroup, interactions:ItemContract.Interactions):ItemContract.Presenter {
        val view = createView(parent)
        parent.addView(view as View)
        return createPresenter(view, interactions)
    }

    fun createPresenter(view: ItemContract.View, interactions: ItemContract.Interactions): ItemContract.Presenter {
        val itemPresenter = ItemPresenter(view, interactions, ItemState())
        view.setPresenter(itemPresenter)
        return itemPresenter
    }

    fun createView(parent: ViewGroup): ItemContract.View {
        return ItemView(parent.context, null)
    }

}
