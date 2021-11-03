package uk.co.sentinelweb.cuer.app.ui.playlist.item

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder

class ItemPresenter(
    val view: ItemContract.View,
    val interactions: ItemContract.Interactions,
    val state: ItemContract.State,
    private val modelMapper: ItemModelMapper,
    private val ytContext: ChromecastYouTubePlayerContextHolder
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
        view.setPlayIcon(
            if (ytContext.isConnected()) {
                R.drawable.ic_notif_status_cast_conn_white
            } else {
                R.drawable.ic_play_black
            }
        )
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

    override fun isCompositePlaylist(): Boolean = state.item!!.playlistName != null

    override fun isStarred(): Boolean = state.item!!.starred

    override fun canDragLeft(): Boolean = state.item?.canDelete ?: false

    override fun canDragRight(): Boolean = state.item?.canEdit ?: false

    override fun canReorder(): Boolean = state.item?.canReorder ?: false

    override fun updateProgress() {
        view.setProgress(state.item?.progress ?: 0f)
    }

    override fun isViewForId(id: Long): Boolean = state.item?.id == id

    override fun doPlay(external: Boolean) {
        interactions.onPlay(state.item!!, external)
    }

    override fun doGotoPlaylist() {
        interactions.onGotoPlaylist(state.item!!)
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

    override fun doRelated() {
        interactions.onRelated(state.item!!)
    }

    override fun doIconClick() {
        interactions.onItemIconClick(state.item!!)
    }

    override fun doPlayStartClick() {
        interactions.onPlayStartClick(state.item!!)
    }

}
