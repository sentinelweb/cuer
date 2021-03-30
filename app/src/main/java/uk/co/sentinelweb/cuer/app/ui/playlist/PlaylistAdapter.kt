package uk.co.sentinelweb.cuer.app.ui.playlist

import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemDiffCallback
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemView
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemViewHolder


class PlaylistAdapter constructor(
    private val itemFactory: ItemFactory,
    private val interactions: ItemContract.Interactions
) : RecyclerView.Adapter<ItemViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    private var _data: MutableList<ItemContract.Model> = mutableListOf()

    val data: List<ItemContract.Model>
        get() = _data

    var highlightItem: Int? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun setData(data: List<ItemContract.Model>, animate: Boolean = true) {
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
            notifyDataSetChanged()
        }

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ItemViewHolder {
        return itemFactory.createItemViewHolder(parent, interactions)
    }

    @Override
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder
            .itemPresenter
            .update(_data.get(position), position == highlightItem)
    }

    override fun getItemCount(): Int = _data.size

    fun getItemViewForId(id: Long): ItemView? {
        recyclerView.children.forEach { childView ->
            if (childView is ItemView) {
                if (childView.isViewForId(id)) {
                    return@getItemViewForId childView
                }
            }
        }
        return null
    }

    fun updateItemModel(model: ItemContract.Model) {
        data.indexOfFirst { it.id == model.id }
            .takeIf { it > -1 }
            ?.also { _data.set(it, model) }
            ?.also { notifyItemChanged(it) }
    }
}
