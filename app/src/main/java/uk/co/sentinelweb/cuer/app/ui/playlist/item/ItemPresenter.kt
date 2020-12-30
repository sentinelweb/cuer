package uk.co.sentinelweb.cuer.app.ui.playlist.item

import uk.co.sentinelweb.cuer.app.R

class ItemPresenter(
    val view: ItemContract.View,
    val interactions: ItemContract.Interactions,
    val state: ItemState
) : ItemContract.Presenter, ItemContract.External {

    override fun update(
        item: ItemContract.PlaylistItemModel,
        highlightPlaying: Boolean
    ) {
        view.setTopText((if (highlightPlaying) "> " else "") + item.title)
        view.setBottomText(item.url)
        view.setCheckedVisible(false)
        item.thumbNailUrl
            ?.apply { view.setIconUrl(this) }
            ?: view.setIconResource(R.drawable.ic_platform_youtube_24_black)
        //view.setBackground(if (highlightPlaying) R.color.playing_item_background else R.color.white)
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

    override fun doPlayStartClick() {
        interactions.onPlayStartClick(state.item!!)
    }

}
