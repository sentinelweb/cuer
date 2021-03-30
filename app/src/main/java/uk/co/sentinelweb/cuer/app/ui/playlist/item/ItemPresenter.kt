package uk.co.sentinelweb.cuer.app.ui.playlist.item

import uk.co.sentinelweb.cuer.app.R

class ItemPresenter(
    val view: ItemContract.View,
    val interactions: ItemContract.Interactions,
    val state: ItemContract.State,
    private val modelMapper: ItemModelMapper
) : ItemContract.Presenter, ItemContract.External {

    override fun update(
        item: ItemContract.Model,
        highlightPlaying: Boolean
    ) {
        view.setTopText(modelMapper.mapTopText(item, highlightPlaying))
        view.setBottomText(modelMapper.mapBottomText(item))
        view.setCheckedVisible(false)
        item.thumbNailUrl
            ?.apply { view.setIconUrl(this) }
            ?: view.setIconResource(R.drawable.ic_platform_youtube_24_black)
        view.setDuration(item.duration)
        view.setDurationBackground(item.infoTextBackgroundColor)
        view.showProgress(!item.isLive)
        if (!item.isLive) {
            view.setProgress(item.progress)
        }
        view.dismissMenu()
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

    override fun updateProgress() {
        view.setProgress(state.item?.progress ?: 0f)
    }

    override fun isViewForId(id: Long): Boolean = state.item?.id == id

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
