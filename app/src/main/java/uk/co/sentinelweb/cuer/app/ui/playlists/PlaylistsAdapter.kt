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

    private var _recyclerView: RecyclerView? = null
    private val recyclerView: RecyclerView
        get() = _recyclerView ?: throw IllegalStateException("PlaylistsAdapter._recyclerView not bound")


    private var _data: List<PlaylistsItemMviContract.Model> = listOf()

    val data: List<PlaylistsItemMviContract.Model>
        get() = _data

    var currentPlaylistId: OrchestratorContract.Identifier<*>? = null

    fun setData(data: List<PlaylistsItemMviContract.Model>, animate: Boolean = true) {
        // todo something in diffutil fails here deleting a parent playlist shog the child after it fails
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
        this._recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this._recyclerView = null
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
            is PlaylistsItemMviContract.Model.ItemModel -> ROW.ordinal
            is PlaylistsItemMviContract.Model.HeaderModel -> HEADER.ordinal
            is PlaylistsItemMviContract.Model.ListModel -> LIST.ordinal
        }

    @Override
    override fun onBindViewHolder(holderRow: RecyclerView.ViewHolder, position: Int) {
        when (holderRow) {
            is ItemRowViewHolder -> _data[position].apply {
                holderRow.itemPresenter.update(
                    this as PlaylistsItemMviContract.Model.ItemModel,
                    currentPlaylistId
                )
            }
            is HeaderViewHolder -> _data[position].apply {
                holderRow.update(
                    this as PlaylistsItemMviContract.Model.HeaderModel
                )
            }
            is ListViewHolder -> _data[position].apply {
                holderRow.listPresenter.update(
                    this as PlaylistsItemMviContract.Model.ListModel,
                    currentPlaylistId
                )
            }
        }
    }

    override fun getItemCount(): Int = _data.size

//    fun getItemViewForId(playlistId: Long?, parentId: Long?): Any {
//        recyclerView.children.forEach { childView ->
//            when (childView.tag) {
//                LIST -> if (parentId != null) {
//
//                }
//                ROW ->
//            }
////            if (childView is ItemRowView) {
////                if (childView.isViewForId(playlistId)) {
////                    return@getItemViewForId childView
////                }
////            }
//        }
//        return null
//    }
}
