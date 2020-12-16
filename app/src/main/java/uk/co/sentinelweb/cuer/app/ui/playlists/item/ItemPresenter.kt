package uk.co.sentinelweb.cuer.app.ui.playlists.item

import uk.co.sentinelweb.cuer.app.R

class ItemPresenter(
    val view: ItemContract.View,
    val interactions: ItemContract.Interactions,
    val state: ItemState
) : ItemContract.Presenter, ItemContract.External {

    override fun update(item: ItemModel) {
        view.setTopText(item.title)
        view.setBottomText(item.data)
        view.setCheckedVisible(item.checkIcon)
        item.thumbNailUrl
            ?.apply { view.setIconUrl(this) }
            ?: R.drawable.ic_chip_playlist_black
                .apply { view.setIconResource(this) }
            ?: view.setIconResource(0)
        state.item = item
    }

    override fun doClick() {
        interactions.onClick(state.item!!)
    }

    override fun doLeft() {
        interactions.onLeftSwipe(state.item!!)
    }

    override fun doRight() {
        interactions.onRightSwipe(state.item!!)
    }

    override fun doPlay(external: Boolean) {
        interactions.onPlay(state.item!!, external)
    }

//    override fun doShowChannel() {
//        interactions.onShowChannel(state.item!!)
//    }

    override fun doStar() {
        interactions.onStar(state.item!!)
    }

    override fun doShare() {
        interactions.onShare(state.item!!)
    }

}
