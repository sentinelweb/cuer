package uk.co.sentinelweb.cuer.app.ui.playlist

import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemDiffCallback
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemViewHolder
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID


class PlaylistAdapter constructor(
    private val itemFactory: ItemFactory,
    private val interactions: ItemContract.Interactions,
    private val showCards: Boolean,
    private val log: LogWrapper,
) : RecyclerView.Adapter<ItemViewHolder>() {

    init {
        log.tag(this)
    }

    private var _recyclerView: RecyclerView? = null
    private val recyclerView: RecyclerView
        get() = _recyclerView ?: throw IllegalStateException("PlaylistAdapter._recyclerView not bound")


    private var _data: MutableList<PlaylistItemMviContract.Model.Item> = mutableListOf()

    val data: List<PlaylistItemMviContract.Model.Item>
        get() = _data

    var playingItem: Int? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun setData(data: List<PlaylistItemMviContract.Model.Item>, animate: Boolean = true) {
        if (animate) {
            DiffUtil.calculateDiff(
                ItemDiffCallback(
                    data,
                    this._data
                )
            ).apply {
                this@PlaylistAdapter._data = data.toMutableList()
                // this stops random scrolling out of view
                val recyclerViewState = recyclerView.layoutManager?.onSaveInstanceState()
                dispatchUpdatesTo(this@PlaylistAdapter)
                recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
            }
        } else {
            this@PlaylistAdapter._data = data.toMutableList()
            // https://github.com/sentinelweb/cuer/issues/369
            if (!recyclerView.isComputingLayout) {
                notifyDataSetChanged()
            } else {
                log.e("Could not update playlist adapter: isComputingLayout", IllegalStateException())
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this._recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this._recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ItemViewHolder {
        return itemFactory.createItemViewHolder(parent, showCards, interactions)
    }

    @Override
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder
            .itemPresenter
            .update(_data.get(position), position == playingItem)
    }

    override fun getItemCount(): Int = _data.size

    fun getItemViewForId(id: Identifier<GUID>): ItemContract.View? {
        recyclerView.children.forEach { childView ->
            if (childView is ItemContract.View) {
                if (childView.isViewForId(id)) {
                    return@getItemViewForId childView
                }
            }
        }
        return null
    }

    fun updateItemModel(model: PlaylistItemMviContract.Model.Item) {
        data.indexOfFirst { it.id == model.id }
            .takeIf { it > -1 }
            ?.also { _data.set(it, model) }
            ?.also { notifyItemChanged(it) }
    }
}
