package uk.co.sentinelweb.cuer.app.ui.playlist.item

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistItemMviContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.GUID

class ItemPresenter(
    val view: ItemContract.View,
    val interactions: ItemContract.Interactions,
    val state: ItemContract.State,
    private val textMapper: ItemTextMapper,
    private val ytContext: ChromecastYouTubePlayerContextHolder,
    private val res: ResourceWrapper
) : ItemContract.Presenter, ItemContract.External {

    override fun update(
        item: PlaylistItemMviContract.Model.Item,
        highlightPlaying: Boolean
    ) {
        view.setTopText(textMapper.mapTopText(item, highlightPlaying))
        view.setBottomText(textMapper.mapBottomText(item))
        view.setCheckedVisible(false)
        item.imageUrl
            ?.apply { view.setImageUrl(this) }
            ?: view.setImageResource(R.drawable.ic_platform_youtube)
        item.thumbUrl
            ?.apply { view.setThumbUrl(this) }
            ?: view.setThumbResource(R.drawable.ic_platform_youtube)
        item.channelImageUrl
            ?.apply { view.setChannelImageUrl(this) }
            ?: view.setChannelImageResource(R.drawable.ic_platform_youtube)
        view.setDuration(item.duration)
        view.setDurationBackground(res.getColorResourceId(item.infoTextBackgroundColor))
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
        view.setShowOverflow(item.showOverflow)
        item.deleteResources?.apply { view.setDeleteResources(this) }
        state.item = item
    }

    override fun doClick() {
        interactions.onClick(state.item!!)
    }

    override fun doAuthorClick() {
        interactions.onShowChannel(state.item!!)
    }

    override fun doLeft() {
        interactions.onLeftSwipe(state.item!!)
    }

    override fun doRight() {
        interactions.onRightSwipe(state.item!!)
    }

    override fun isCompositePlaylist(): Boolean = state.item!!.playlistName != null

    override fun isStarred(): Boolean = state.item!!.isStarred

    override fun canDragLeft(): Boolean = state.item?.canDelete ?: false

    override fun canDragRight(): Boolean = state.item?.canEdit ?: false

    override fun canReorder(): Boolean = state.item?.canReorder ?: false

    override fun updateProgress() {
        view.setProgress(state.item?.progress ?: 0f)
    }

    override fun isViewForId(id: OrchestratorContract.Identifier<GUID>): Boolean = state.item?.id == id

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
