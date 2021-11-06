package uk.co.sentinelweb.cuer.app.ui.playlists.item

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract.Model.ItemModel

class ItemPresenter(
    val view: ItemContract.View,
    val interactions: ItemContract.Interactions,
    val state: ItemContract.State,
    val modelMapper: ItemModelMapper
) : ItemContract.Presenter, ItemContract.External<ItemModel> {

    override fun update(
        item: ItemModel,
        current: OrchestratorContract.Identifier<*>?
    ) {
        view.setTopText(modelMapper.mapTopText(item, current?.id == item.id))
        view.setBottomText(modelMapper.mapBottomText(item))
        view.setCheckedVisible(item.checkIcon)
        item.thumbNailUrl
            ?.apply { view.setIconUrl(this) }
            ?: R.drawable.ic_playlist_black
                .apply { view.setIconResource(this) }
        state.item = item
        view.showOverflow(item.showOverflow && (canPlay() || canLaunch() || canEdit() || canShare()))
    }

    override fun doImageClick() {
        interactions.onImageClick(state.item!!)
    }

    override fun doEdit() {
        interactions.onEdit(state.item!!)
    }

    override fun isStarred(): Boolean = state.item!!.starred

    override fun doMerge() {
        interactions.onMerge(state.item!!)
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

    override fun canDragLeft(): Boolean = state.item?.canDelete ?: false

    override fun canDragRight(): Boolean = state.item?.canEdit ?: false

    override fun canReorder(): Boolean = state.item?.canEdit ?: false

    override fun doPlay(external: Boolean) {
        interactions.onPlay(state.item!!, external)
    }

    override fun doStar() {
        interactions.onStar(state.item!!)
    }

    override fun doShare() {
        interactions.onShare(state.item!!)
    }

    override fun canEdit(): Boolean = state.item?.canEdit ?: false

    override fun canShare(): Boolean = state.item?.canShare ?: false

    override fun canPlay(): Boolean = state.item?.canPlay ?: false

    override fun canLaunch(): Boolean = state.item?.canLaunch ?: false


}
