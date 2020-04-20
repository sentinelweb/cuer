package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemModel

interface PlaylistContract {
    interface Presenter {
        fun initialise()
        fun loadList()
    }

    interface View {

        fun setList(list: List<ItemModel>)
    }
}