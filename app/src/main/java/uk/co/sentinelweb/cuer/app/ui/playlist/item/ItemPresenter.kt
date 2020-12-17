package uk.co.sentinelweb.cuer.app.ui.playlist.item

import uk.co.sentinelweb.cuer.app.R

class ItemPresenter(
    val view: ItemContract.View,
    val interactions: ItemContract.Interactions,
    val state: ItemState
) : ItemContract.Presenter, ItemContract.External {

    override fun update(
        item: ItemModel,
        highlightPlaying: Boolean
    ) {
        view.setTopText(item.topText)
        view.setBottomText(item.bottomText)
        view.setCheckedVisible(item.checkIcon)
        item.thumbNailUrl
            ?.apply { view.setIconUrl(this) }
            ?: item.iconRes
                ?.apply { view.setIconResource(this) }
            ?: view.setIconResource(0)
        view.setBackground(if (highlightPlaying) R.color.playing_item_background else R.color.white)
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

    override fun doShowChannel() {
        interactions.onShowChannel(state.item!!)
    }

    override fun doStar() {
        interactions.onStar(state.item!!)
    }

    override fun doShare() {
        interactions.onShare(state.item!!)
    }

    override fun doView() {
        interactions.onView(state.item!!)
    }

}
