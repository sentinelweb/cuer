package uk.co.sentinelweb.cuer.app.ui.playlists

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemDiffCallback
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract.ItemType.*
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.header.HeaderViewHolder
import uk.co.sentinelweb.cuer.app.ui.playlists.item.list.ListViewHolder
import uk.co.sentinelweb.cuer.app.ui.playlists.item.row.ItemRowViewHolder


class PlaylistsAdapter constructor(
    private val itemFactory: ItemFactory,
    private val interactions: ItemContract.Interactions
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    private var _data: List<ItemContract.Model> = listOf()

    val data: List<ItemContract.Model>
        get() = _data

    var currentPlaylistId: OrchestratorContract.Identifier<*>? = null

    fun setData(data: List<ItemContract.Model>, animate: Boolean = true) {
        if (animate) {
            DiffUtil.calculateDiff(
                ItemDiffCallback(
                    data,
                    this._data
                )
            ).apply {
                this@PlaylistsAdapter._data = data
                // this stops random scrolling out of view
                val recyclerViewState = recyclerView.layoutManager?.onSaveInstanceState()
                dispatchUpdatesTo(this@PlaylistsAdapter)
                recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
            }
        } else {
            this@PlaylistsAdapter._data = data
            notifyDataSetChanged()
        }

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
        return when (type) {
            ROW.ordinal -> itemFactory.createItemViewHolder(parent, interactions)
            HEADER.ordinal -> itemFactory.createHeaderViewHolder(parent)
            LIST.ordinal -> itemFactory.createListViewHolder(parent, interactions)
            else -> throw IllegalArgumentException("Tile not supported")
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (data[position]) {
            is ItemContract.Model.ItemModel -> ROW.ordinal
            is ItemContract.Model.HeaderModel -> HEADER.ordinal
            is ItemContract.Model.ListModel -> LIST.ordinal
        }

    @Override
    override fun onBindViewHolder(holderRow: RecyclerView.ViewHolder, position: Int) {
        when (holderRow) {
            is ItemRowViewHolder -> _data[position].apply {
                holderRow.itemPresenter.update(
                    this as ItemContract.Model.ItemModel,
                    currentPlaylistId
                )
            }
            is HeaderViewHolder -> _data[position].apply {
                holderRow.update(
                    this as ItemContract.Model.HeaderModel
                )
            }
            is ListViewHolder -> _data[position].apply {
                holderRow.listPresenter.update(
                    this as ItemContract.Model.ListModel,
                    currentPlaylistId
                )
            }
        }
    }

    override fun getItemCount(): Int = _data.size
}
